
package com.xiaomi.xms.sales.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import java.util.HashMap;

public class OrderEditDeliverTimeFragment extends BaseFragment {
    private static final int REQUEST_LOADER = 0;

    private RequestLoader mLoader;
    private RadioGroup mRadioGroup;
    private Button mSubmit;
    private String mOldDeliverTime;
    private String mOrderId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_edit_delivertime_fragment, container, false);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.deliver_time);
        mSubmit = (Button) view.findViewById(R.id.address_submit);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSubmit.setOnClickListener(mOnSubmitListener);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOldDeliverTime = bundle.getString(Constants.Intent.EXTRA_ORDER_DELIVER_TIME);
            mOrderId = bundle.getString(Constants.Intent.EXTRA_PAYMENT_ORDER_ID);
        }
        initButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.order_edit_deliver_time_title);
    }

    private static HashMap<String, String> mDeliverArr = new HashMap<String, String>();
    static {
        mDeliverArr.put("1", "不限");
        mDeliverArr.put("2", "工作日送货(适用于办公地址)");
        mDeliverArr.put("3", "双休日、假日送货(适合于家庭地址)");
    };

    private void initButtons() {
        int count = mDeliverArr.size();
        for (int i = 1; i <= count; i++) {
            String key = String.valueOf(i);
            RadioButton button = new RadioButton(getActivity());
            button.setText(mDeliverArr.get(key));
            button.setTag(key);
            mRadioGroup.addView(button);
            if (count == 1) {
                button.setBackgroundResource(R.drawable.radiobutton_single_bg);
            } else if (i == 1) {
                button.setBackgroundResource(R.drawable.radiobutton_up_bg);
            } else if (i == count) {
                button.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
            } else {
                button.setBackgroundResource(R.drawable.radiobutton_middle_bg);
            }
            button.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            if (TextUtils.equals(mOldDeliverTime, mDeliverArr.get(key))) {
                button.setChecked(true);
            }
        }
    }

    private OnClickListener mOnSubmitListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            int id = mRadioGroup.getCheckedRadioButtonId();
            RadioButton button = (RadioButton) mRadioGroup.findViewById(id);
            String best_time = (String) button.getTag();
            // TODO
            getLoaderManager().initLoader(REQUEST_LOADER, null, mRequestCallback);
            if (mLoader != null) {
                Request request = new Request(HostManager.getEditOrder());
                request.addParam(Tags.EditOrder.USER_ID, LoginManager.getInstance().getUserId());
                request.addParam(Tags.EditOrder.ORDER_ID, mOrderId);
                request.addParam(Tags.EditOrder.TYPE, Tags.EditOrder.VALUE_TYPE_TIME);
                request.addParam(Tags.EditOrder.BEST_TIME, best_time);
                mLoader.load(REQUEST_LOADER, request);
            }
        }
    };

    private Handler mHandler = new Handler();
    private LoaderCallbacks<RequestLoader.Result> mRequestCallback = new LoaderCallbacks<RequestLoader.Result>() {
        @Override
        public void onLoaderReset(Loader<Result> loader) {
        }

        @Override
        public void onLoadFinished(Loader<Result> loader, Result result) {
            getLoaderManager().destroyLoader(REQUEST_LOADER);
            if (result != null) {
                if (Tags.isJSONResultOK(result.mData)) {
                    ToastUtil.show(getActivity(), R.string.order_edit_ok);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().onBackPressed();
                        }
                    });
                } else {
                    ToastUtil.show(getActivity(), result.mData.optString(Tags.DESCRIPTION));
                }
            }
        }

        @Override
        public Loader<Result> onCreateLoader(int type, Bundle bundle) {
            mLoader = new RequestLoader(getActivity());
            return mLoader;
        }
    };
}
