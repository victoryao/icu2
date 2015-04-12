
package com.xiaomi.xms.sales.widget;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;


public class BaseAlertDialog extends Dialog {
    public static final int BUTTON_POSITIVE = R.id.positive;
    public static final int BUTTON_NEGATIVE = R.id.negative;

    private TextView mTitle;
    private ImageView mIcon;
    private TextView mMessage;
    private Button mPositiveBtn;
    private Button mNegativeBtn;
    private View mButtonPadding;

    public BaseAlertDialog(Context context) {
        super(context, R.style.Widget_Dialog);
        setContentView(R.layout.base_dialog);
        mTitle = (TextView) findViewById(R.id.title);
        mIcon = (ImageView) findViewById(R.id.icon);
        mMessage = (TextView) findViewById(R.id.message);
        mPositiveBtn = (Button) findViewById(R.id.positive);
        mNegativeBtn = (Button) findViewById(R.id.negative);
        mButtonPadding = findViewById(R.id.button_padding);
    }

    public void setTitle(int resId) {
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(resId);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(title);
    }

    public void setIcon(int resId) {
        mIcon.setVisibility(View.VISIBLE);
        mIcon.setImageResource(resId);
    }

    public void setMessage(int resId) {
        setMessage(getContext().getString(resId));
    }

    public void setMessage(CharSequence msg) {
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(msg);
    }

    public void setView(View view) {
        ViewGroup content = (ViewGroup) findViewById(R.id.content);
        content.removeAllViews();
        content.addView(view);
    }

    public void setPositiveButton(int resId, View.OnClickListener listener) {
        mPositiveBtn.setVisibility(View.VISIBLE);
        if (mNegativeBtn.getVisibility() == View.VISIBLE) {
            mButtonPadding.setVisibility(View.VISIBLE);
        }

        if (resId > 0) {
            mPositiveBtn.setText(resId);
        }

        if (listener != null) {
            mPositiveBtn.setOnClickListener(new OnClickListenerWrapper(listener));
        } else {
            mPositiveBtn.setOnClickListener(mCloseListener);
        }
    }

    public void setPositiveButton(int resId, View.OnClickListener listener, boolean autoDismiss) {
        mPositiveBtn.setVisibility(View.VISIBLE);
        if (mNegativeBtn.getVisibility() == View.VISIBLE) {
            mButtonPadding.setVisibility(View.VISIBLE);
        }

        if (resId > 0) {
            mPositiveBtn.setText(resId);
        }

        if (listener != null) {
            if (autoDismiss) {
                mPositiveBtn.setOnClickListener(new OnClickListenerWrapper(listener));
            } else {
                mPositiveBtn.setOnClickListener(listener);
            }
        } else {
            mPositiveBtn.setOnClickListener(mCloseListener);
        }
    }

    public void setNegativeButton(int resId, View.OnClickListener listener) {
        mNegativeBtn.setVisibility(View.VISIBLE);

        if (mPositiveBtn.getVisibility() == View.VISIBLE) {
            mButtonPadding.setVisibility(View.VISIBLE);
        }

        if (resId > 0) {
            mNegativeBtn.setText(resId);
        }

        if (listener != null) {
            mNegativeBtn.setOnClickListener(new OnClickListenerWrapper(listener));
        } else {
            mNegativeBtn.setOnClickListener(mCloseListener);
        }
    }

    private View.OnClickListener mCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private class OnClickListenerWrapper implements View.OnClickListener {
        private View.OnClickListener mListener;

        public OnClickListenerWrapper(View.OnClickListener l) {
            mListener = l;
        }

        @Override
        public void onClick(View v) {
            dismiss();
            if (mListener != null) {
                mListener.onClick(v);
            }
        }
    }

}
