
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.FCodeActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;

public class FCodeFragment extends BaseFragment {
    private EditText mFcodeText;
    private EditText mVcodeText;
    private ImageView mVerifyImage;
    private Button mChangeBtn;
    private Button mSubmitBtn;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fcode_fragment, container, false);
        mFcodeText = (EditText) view.findViewById(R.id.fcode);
        mVcodeText = (EditText) view.findViewById(R.id.vcode);
        mVerifyImage = (ImageView) view.findViewById(R.id.vcode_image);
        mChangeBtn = (Button) view.findViewById(R.id.change);
        mSubmitBtn = (Button) view.findViewById(R.id.submit);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String fcode = bundle.getString(Constants.Intent.EXTRA_CHECKCODE_FCODE);
            if (!TextUtils.isEmpty(fcode)) {
                mFcodeText.setText(fcode);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flushVerifyCode();
        mProgressDialog = ProgressDialog.show(getActivity(), null,
                getResources().getString(R.string.vcode_waiting), false, true);
        mChangeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVcodeText.setText("");
                mVcodeText.requestFocus();
                flushVerifyCode();
            }
        });
        mSubmitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void flushVerifyCode() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_FETCH_VCODE);
        activity.startService(intent);
    }

    private void submit() {
        mProgressDialog = ProgressDialog.show(getActivity(), null,
                getResources().getString(R.string.fcode_waiting), false, true);
        Activity activity = getActivity();
        Intent intent = new Intent(activity, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_VERIFY_VCODE);
        intent.putExtra(Constants.Intent.EXTRA_CHECKCODE_VCODE, mVcodeText.getText().toString());
        activity.startService(intent);
    }

    public void onServiceCompleted(String action, Intent callbackIntent) {
        if (Constants.Intent.ACTION_FETCH_VCODE.equals(action)) {
            onFetchVcodeCompleted(callbackIntent);
        } else if (Constants.Intent.ACTION_VERIFY_VCODE.equals(action)) {
            onVerifyVcodeCompleted(callbackIntent);
        } else if (Constants.Intent.ACTION_VERIFY_FCODE.equals(action)) {
            onVerifyFcodeCompleted(callbackIntent);
        }
    }

    private void onFetchVcodeCompleted(Intent intent) {
        String url = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_URL);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (url != null) {
            ImageLoader.getInstance()
                    .loadImage(mVerifyImage, new Image(url), R.drawable.list_default_bg);
        } else {
            ToastUtil.show(getActivity(), R.string.fcode_vcode_fetch_err);
        }
    }

    private void onVerifyVcodeCompleted(Intent intent) {
        boolean isOK = intent.getBooleanExtra(Constants.Intent.EXTRA_CHECKCODE_RESULT, false);
        if (isOK) {
            Activity activity = getActivity();
            Intent verifyIntent = new Intent(activity, ShopIntentService.class);
            verifyIntent.setAction(Constants.Intent.ACTION_VERIFY_FCODE);
            verifyIntent.putExtra(Constants.Intent.EXTRA_CHECKCODE_FCODE, mFcodeText.getText()
                    .toString());
            activity.startService(verifyIntent);
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mVcodeText.requestFocus();
            ToastUtil.show(getActivity(), R.string.fcode_vcode_err);
        }
    }

    private void onVerifyFcodeCompleted(Intent intent) {
        boolean isOK = intent.getBooleanExtra(Constants.Intent.EXTRA_CHECKCODE_RESULT, false);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (isOK) {
            String json = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_LISTSTR);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_CHECKCODE_LISTSTR, json);
            ((BaseActivity) getActivity()).showFragment(FCodeActivity.TAG_SELECT_FRAGMENT,
                    bundle, true);
        } else {
            mFcodeText.requestFocus();
            String msg = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_MESSAGE);
            if (TextUtils.isEmpty(msg)) {
                ToastUtil.show(getActivity(), R.string.fcode_verify_fcode_err);
            } else {
                ToastUtil.show(getActivity(), msg);
            }
        }
    }
}
