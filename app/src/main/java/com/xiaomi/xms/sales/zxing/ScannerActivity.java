/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.xms.sales.zxing;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ICUMainActivity;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.zxing.camera.CameraManager;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class ScannerActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private TextView statusView;
    private boolean hasSurface;;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private ShopIntentServiceAction mUpdateShoppingCountAction;
    private int mMihomeShoppingCount = Constants.UNINITIALIZED_NUM;
    private View mHomeButton;
    private TextView mTitle;
    private View mShoppingStatusBar;
    private LinearLayout cart;
    private boolean isScannerOk;
    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        isScannerOk = false;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);
        cart = (LinearLayout)findViewById(R.id.title_right_bar);
        mShoppingStatusBar = findViewById(R.id.title_bar_custom_view);
        statusView = (TextView)findViewById(R.id.status_view);
        mShoppingStatusBar.setOnClickListener(mClickListener);
        mHomeButton = findViewById(R.id.title_bar_home);
        mHomeButton.setOnClickListener(mClickListener);
        
       
        mTitle = (TextView) findViewById(R.id.title_bar_title);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScannerOk = false;
        // CameraManager must be initialized here, not in onCreate().
        // This is necessary because we don't want to open the camera driver and
        // measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially off screen.
        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        resetStatusView();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        Intent intent = getIntent();
        if (intent != null) {
            decodeHints = DecodeHintManager.parseDecodeHints(intent);
        }
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore surfaceCreated() won't be called, so init the
            // camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
        }
        beepManager.updatePrefs();
        inactivityTimer.onResume();
        if (TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_MIHOME_SCANNER)) {
            mTitle.setText(R.string.scanner_mihome_title);
            statusView.setText(R.string.scanner_mihome_prompt);
        } else if (TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_PRODUCT_SCANNER)) {
            mTitle.setText(R.string.account_nfc_add_text);
            statusView.setText(R.string.scanner_product_prompt);
            updateMihomeShoppingCount();
        } else if (TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_PRODUCT_SCAN)) {
            mTitle.setText(R.string.scanner_product_title);
            statusView.setText(R.string.scanner_product_prompt);
            updateMihomeShoppingCount();
        }
        else if(TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_XIANHUO_SCAN)){ //现货销售
        	 mTitle.setText(R.string.scanner_product_title);
             statusView.setText(R.string.scanner_product_prompt);

     	    cart.setVisibility(View.GONE);
        }
        statusView.setText(R.string.scanner_product_prompt);
        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.title_bar_custom_view:
                    if (LoginManager.getInstance().hasLogin()) {
                        gotoShoppingCart();
                    } else {
                        ToastUtil.show(ScannerActivity.this, R.string.login_before_check_shopping_cart);
                        gotoAccount();
                    }
                    break;
                case R.id.title_bar_home:
                    onBackPressed();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            	isScannerOk = true;
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
                // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void updateMihomeShoppingCount() {
        if (mUpdateShoppingCountAction == null) {
            mUpdateShoppingCountAction = new ShopIntentServiceAction(
                    Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT, this);
        }
        ShopIntentService.registerAction(mUpdateShoppingCountAction);
        Intent intent = new Intent(this, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT);
        startService(intent);

    }

    @Override
    public void onServiceCompleted(String action, Intent callbackIntent) {
        super.onServiceCompleted(action, callbackIntent);
        if (Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT.equals(action)) {
            ShopIntentService.unregisterAction(mUpdateShoppingCountAction);
            mMihomeShoppingCount = callbackIntent.getIntExtra(
                    Constants.Intent.EXTRA_SHOPPING_COUNT, Constants.UNINITIALIZED_NUM);
            updateShoppingCountView();
        }
    }

    private void updateShoppingCountView() {
    	
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            LogUtil.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     * 
     * @param rawResult The contents of the barcode.
     * @param barcode A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {
    	isScannerOk = true;   //扫描到了
    	try {
			LogHelper.getInstance(getApplication()).save("",Constants.LogType.XIANHUO_SCANNER_START,"","");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        String resultString = rawResult.getText().trim();
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(ScannerActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        BarcodeFormat barcodeFormat = rawResult.getBarcodeFormat();
        if (barcodeFormat == BarcodeFormat.QR_CODE || barcodeFormat == BarcodeFormat.MAXICODE) {
            // 扫描的是二维码 2D
            if (TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_PATIENT_SEARCH_SCAN)) {
                Intent intent = new Intent(this, ICUMainActivity.class);
                intent.putExtra(Constants.Intent.EXTRA_PATIENT_SN_STR, resultString);
                intent.setAction(Constants.Intent.ACTION_PATIENT_SEARCH_SCAN);
                startActivity(intent);
                finish();
            } 
        } else {
            barCodeHandler(resultString);
				
        }
    }

	private void barCodeHandler(String resultString) {
		// 扫描的是条形码 1D
		if (TextUtils.equals(getIntent().getAction(), Constants.Intent.ACTION_PATIENT_SEARCH_SCAN)) { 
			Intent intent = new Intent();
			intent.setClass(this, ICUMainActivity.class);
			intent.putExtra(Constants.Intent.EXTRA_PATIENT_SN_STR, resultString);
			intent.setAction(Constants.Intent.ACTION_PATIENT_SEARCH_SCAN);
			startActivity(intent);
			finish();
		}

	}

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features
     * ofthe barcode.
     * 
     * @param barcode A bitmap of the captured image.
     * @param rawResult The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                    rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            LogUtil.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            LogUtil.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            LogUtil.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
