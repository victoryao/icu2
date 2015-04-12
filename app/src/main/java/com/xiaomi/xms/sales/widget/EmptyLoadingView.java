
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Handler.Callback;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.NetworkErrorActivity;
import com.xiaomi.xms.sales.loader.BaseResult;
import com.xiaomi.xms.sales.loader.ProgressNotifiable;
import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;

public class EmptyLoadingView extends LinearLayout implements
        ProgressNotifiable {
    private static final String TAG = "EmptyLoadingView";
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private Button mButton;
    private int mEmptyResId;
    private CharSequence mEmptyText;

    public EmptyLoadingView(Context context) {
        this(context, null);
    }

    public EmptyLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.empty_loading, this, true);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mTextView = (TextView) findViewById(R.id.empty_text);
        mButton = (Button) findViewById(R.id.button);
        LogUtil.d("XiaoMiShop-EmptyLoadingView", "init loading view:" + mTextView.getText());
    }

    private void updateStyle(boolean hasData) {
        if (hasData) {
            getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            setBackgroundResource(R.drawable.loading_view_bg);
        } else {
            getLayoutParams().height = LayoutParams.MATCH_PARENT;
            setBackgroundDrawable(null);
        }
    }

    @Override
    public void startLoading(boolean hasData) {
        updateStyle(hasData);
        mProgressBar.setVisibility(VISIBLE);
        mTextView.setVisibility(GONE);
        mButton.setVisibility(GONE);
        showView(this);
    }

    @Override
    public void stopLoading(boolean hasData) {
        updateStyle(hasData);
        if (hasData) {
            hideView(this);
        } else {
            showView(this);
            mProgressBar.setVisibility(GONE);
            mTextView.setVisibility(VISIBLE);
            if (mEmptyResId != 0) {
                mTextView.setText(mEmptyResId);
            } else if (!TextUtils.isEmpty(mEmptyText)) {
                mTextView.setText(mEmptyText);
            }
            mButton.setVisibility(GONE);
        }
    }

    @Override
    public void init(boolean hasData, boolean isLoading) {
        updateStyle(hasData);
        if (isLoading) {
            setVisibility(VISIBLE);
            mProgressBar.setVisibility(VISIBLE);
            mTextView.setVisibility(GONE);
            mButton.setVisibility(GONE);
        } else {
            if (hasData) {
                setVisibility(GONE);
            } else {
                setVisibility(VISIBLE);
                mProgressBar.setVisibility(GONE);
                mTextView.setVisibility(VISIBLE);
                mButton.setVisibility(GONE);
            }
        }
    }

    private void showView(View view) {
        if (view == null) {
            return;
        }

        if (view.getVisibility() == GONE) {
            view.startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.appear));
            view.setVisibility(VISIBLE);
        }
    }

    private void hideView(View view) {
        if (view == null) {
            return;
        }
        if (view.getVisibility() == VISIBLE) {
            if (view.isShown()) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(),
                        R.anim.disappear));
            }
            view.setVisibility(GONE);
        }
    }

    public void setEmptyText(int emptyRes) {
        mEmptyResId = emptyRes;
    }

    public void setEmptyText(CharSequence errorStr) {
        mEmptyText = errorStr;
    }

    @Override
    public void onError(boolean hasData, ResultStatus status, final Callback callback) {
    	if(status == BaseResult.ResultStatus.NETWROK_ERROR || status == BaseResult.ResultStatus.SERVICE_ERROR){
         	Intent intent = new Intent(getContext(), NetworkErrorActivity.class);
         	getContext().startActivity(intent);
        }
        else{
	        updateStyle(hasData);
	        if (hasData) {
	            hideView(this);
	            ToastUtil.show(getContext(), BaseResult.getStatusDes(status));
	        } else {
            	showView(this);
                mProgressBar.setVisibility(GONE);
                mTextView.setVisibility(VISIBLE);
                mTextView.setText(BaseResult.getStatusDes(status));
                LogUtil.d("XiaoMiShop-EmptyLoadingView", "set err text:" + mTextView.getText());
                mButton.setVisibility(VISIBLE);
                if (status == BaseResult.ResultStatus.NETWROK_ERROR) {
                    mButton.setText(R.string.check_network);
                } else if (status == ResultStatus.AUTH_ERROR) {
                    mButton.setText(R.string.login_again);
                } else {
                    mButton.setText(R.string.try_again);
                }
                mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.handleMessage(null);
                    }
                });
	        }
        }
    }
}
