package com.xiaomi.xms.sales.xmsf.account.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.LogUtil;

public abstract class StepsFragment extends Fragment implements OnClickListener {
    private static final String TAG = "StepsFragment";

    protected Button mButtonPrevious;
    protected Button mButtonNext;
    protected Button mButtonFinish;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtonPrevious = (Button) view.findViewById(R.id.btn_previous);
        mButtonNext = (Button) view.findViewById(R.id.btn_next);
        mButtonFinish = (Button) view.findViewById(R.id.btn_finish);

        if (mButtonPrevious != null) {
            mButtonPrevious.setOnClickListener(this);
        }
        if (mButtonNext != null) {
            mButtonNext.setOnClickListener(this);
        }
        if (mButtonFinish != null) {
            mButtonFinish.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mButtonPrevious) {
            onButtonPreviousClicked();
        } else if (view == mButtonNext) {
            onButtonNextClicked();
        } else if (view == mButtonFinish) {
            onButtonFinishClicked();
        }
    }

    protected void displaySoftInputIfNeed(View focusedView, boolean tryDisplay) {
        // determine whether tryDisplay soft input keyboard or not
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (tryDisplay && getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            imm.showSoftInput(focusedView, 0);
        } else {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public void setButtonPreviousGone(boolean gone) {
        if (mButtonPrevious != null) {
            mButtonPrevious.setVisibility(gone ? View.GONE : View.VISIBLE);
        } else {
            Log.w(TAG, "button previous is null");
        }
    }

    public void setButtonNextGone(boolean gone) {
        if (mButtonNext != null) {
            mButtonNext.setVisibility(gone ? View.GONE : View.VISIBLE);
        } else {
            Log.w(TAG, "button next is null");
        }
    }

    protected void triggerNextStep() {
        onButtonNextClicked();
    }

    protected void replaceToFragment(final Fragment f, final boolean clearBottom) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (clearBottom) {
            int count = manager.getBackStackEntryCount();
            for (int i = 0; i < count; i++) {
                manager.popBackStack();
            }
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment_container, f);
        if (!clearBottom) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
    }

    protected void onButtonPreviousClicked() {
        getFragmentManager().popBackStack();
    }

    protected void onButtonNextClicked() {

    }

    protected void onButtonFinishClicked() {
        LogUtil.d(TAG, "on button finish clicked");
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
