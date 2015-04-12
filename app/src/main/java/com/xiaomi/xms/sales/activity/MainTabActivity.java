
package com.xiaomi.xms.sales.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.SplashUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class MainTabActivity extends BaseActivity {
    private ImageView mBg;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mBg = new ImageView(this);
//        Bitmap img = SplashUtil.getSplashImage();
//        if (img != null) {
//            mBg.setBackgroundDrawable(new BitmapDrawable(img));
//        } else {
////            mBg.setBackgroundResource(R.drawable.default_splash);
//        	mBg.setBackgroundResource(R.drawable.adpic);
//        }
        mBg.setBackgroundResource(R.drawable.adpic);
        setContentView(mBg);
        
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
//                if (LoginManager.getInstance().hasLogin()) {
//                    startActivity(new Intent(MainTabActivity.this, MainActivity.class));
//                } else {
                    MainTabActivity.this.gotoAccount();
//                }
                finish();
            }
        }, 800);
    }
}
