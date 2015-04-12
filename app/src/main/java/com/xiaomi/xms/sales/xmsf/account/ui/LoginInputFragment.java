
package com.xiaomi.xms.sales.xmsf.account.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;

public class LoginInputFragment extends Fragment implements View.OnClickListener,
        LoginFailureFragment.GetUserInputInterface {
    private static final String TAG = "LoginInputFragment";

    private AutoCompleteTextView mAccountNameView;
    private EditText mAccountPwdView;
    private Button mButtonLogin;
    private TextView mForgetPwdView;
    private TextView mRegisterAccountView;
    private OnLoginInterface mOnLoginInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_login, container, false);

        mAccountNameView = (AutoCompleteTextView) v.findViewById(R.id.et_account_name);
        mAccountPwdView = (EditText) v.findViewById(R.id.et_account_password);
        mButtonLogin = (Button) v.findViewById(R.id.btn_login);
        mForgetPwdView = (TextView) v.findViewById(R.id.tv_forget_pwd);
        mRegisterAccountView = (TextView) v.findViewById(R.id.tv_reg_new);
        mAccountPwdView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    startLogin();
                    return true;
                }
                return false;
            }
        });
        mButtonLogin.setOnClickListener(this);
        mForgetPwdView.setOnClickListener(this);
        mRegisterAccountView.setOnClickListener(this);

        String userNames = Utils.Preference.getStringPref(getActivity(), Constants.Account.PREF_USER_NAMES, "");
        if (!TextUtils.isEmpty(userNames)) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.login_username_list_item, userNames.split(Constants.Account.USER_NAME_SEPARATOR));
            mAccountNameView.setThreshold(1);
            mAccountNameView.setAdapter(adapter);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if (TextUtils.isEmpty(mAccountNameView.getText())) {
            mAccountNameView.requestFocus();
        } else {
            mAccountPwdView.requestFocus();
        }
    }

    public void setOnLoginInterface(OnLoginInterface onLoginInterface) {
        mOnLoginInterface = onLoginInterface;
    }

    private SpannableStringBuilder getErrorSpanString(int resId) {
        String err = getActivity().getString(resId);
        TextAppearanceSpan highlightTextAppearanceSpan = new TextAppearanceSpan(
                getActivity(), R.style.TextAppearance_Notice_Normal);
        SpannableStringBuilder builder = new SpannableStringBuilder(err);
        builder.setSpan(highlightTextAppearanceSpan, builder.length()
                - err.length(),
                builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonLogin) {
            boolean hasError = false;
            String username = mAccountNameView.getText().toString();
            String pwd = mAccountPwdView.getText().toString();
            if (TextUtils.isEmpty(username)) {
                hasError = true;

                mAccountNameView
                        .setError(getErrorSpanString(R.string.micloud_error_empty_username));
            }
            if (TextUtils.isEmpty(pwd)) {
                hasError = true;
                mAccountPwdView
                        .setError(getErrorSpanString(R.string.micloud_error_empty_pwd));
            }
            if (!hasError) {
                Utils.SoftInput.hide(getActivity(), mAccountNameView.getWindowToken());
                startLogin();
            }
        } else if (v == mForgetPwdView) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Constants.Account.URL_PASSWORD_RECOVERY));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            getActivity().startActivity(intent);
        } else if (v == mRegisterAccountView) {
            Intent intent = new Intent(getActivity(), RegisterAccountActivity.class);
            startActivityForResult(intent, Constants.RequestCode.CODE_REQUEST_SIGUP);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.SoftInput.hide(getActivity(), mAccountNameView.getWindowToken());
        Utils.SoftInput.hide(getActivity(), mAccountPwdView.getWindowToken());
    }

    private void startLogin() {
        if (mOnLoginInterface != null) {
            mOnLoginInterface
                    .onStartLogin(mAccountNameView.getText().toString(),
                            mAccountPwdView.getText().toString());
        }
    }

    public static interface OnLoginInterface {
        void onStartLogin(String userName, String pwd);
    }

    @Override
    public String getUserName() {
        return mAccountNameView.getText().toString();
    }

    @Override
    public String getUserPwd() {
        return mAccountPwdView.getText().toString();
    }

}
