package com.xiaomi.xms.sales.xmsf.account.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;

public class LoginProgressFragment extends Fragment implements View.OnClickListener {

    private Button mButtonCancel;

    private OnLoginProgressInterface mOnLoginProgressInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_login_progress, container, false);

        mButtonCancel = (Button) v.findViewById(R.id.btn_cancel);
        mButtonCancel.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonCancel) {
            if (mOnLoginProgressInterface != null) {
                mOnLoginProgressInterface.onLoginCanceled();
            }
        }
    }

    public void setOnLoginProgressInterface(OnLoginProgressInterface onLoginProgressInterface) {
        mOnLoginProgressInterface = onLoginProgressInterface;
    }

    public void disableCancel() {
        mButtonCancel.setEnabled(false);
    }

    public static interface OnLoginProgressInterface {
        void onLoginCanceled();
    }
}
