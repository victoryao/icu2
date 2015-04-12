
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentService.Listener;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.ShoppingAdapter;
import com.xiaomi.xms.sales.loader.GetServiceNumberLoader;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ShoppingLoader;
import com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader;
import com.xiaomi.xms.sales.loader.ShoppingLoader.Result;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

public class OrderSubmitFragment extends BaseFragment implements
        LoaderCallbacks<ShoppingLoader.Result> {
    private final static int ORDER_SUBMIT_LOADER = 0;
    private final static int TAG_BACKPRESS_MESSAGE = 1;

    private final static int GET_SERVICENUMBER = 600;
    private final static int WIPE_ZERO = 3;
    private EmptyLoadingView mLoadingView;
    private TextView mCount;
    private Button mSubmit;
    private String mJsonData;
    private ShoppingAdapter mAdapter;
    private BaseListView mListView;
    private ImageView mVcodeImage;
    private EditText mVcodeInput;
    private Button mChangeBtn;
    private boolean mNeedCheckCode;
    private BaseAlertDialog mCheckCodeDialog;
    private ShopIntentServiceAction mFetchDefenseVcodeAction;
    private ProgressDialog mProgressDialog;
    private View mListHeader;
    private View mListFooter;
    private View mBottom;
    private String serviceNumber;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mJsonData = bundle.getString(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ordersubmit_fragment, container, false);
        mBottom = view.findViewById(R.id.bottom);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mListHeader = inflater.inflate(R.layout.use_coupon_header_view, null, false);
        mListFooter = inflater.inflate(R.layout.order_submit_footer, null, false);
        mListView.addHeaderView(mListHeader);
        mListView.addFooterView(mListFooter, null, false);
        mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding),
                0, getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mCount = (TextView) view.findViewById(R.id.count);
        mSubmit = (Button) view.findViewById(R.id.submit);

        mListFooter.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBottom.setVisibility(View.GONE);
        mSubmit.setOnClickListener(mOnButtonClickListener);

        mAdapter = new ShoppingAdapter(getActivity());
        mAdapter.hideArrow(true);
        mAdapter.showTopLine(true);
        mAdapter.showPaperBackground();
        mListView.setAdapter(mAdapter);

        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == ORDER_SUBMIT_LOADER) {
            mLoader = new ShoppingLoader(getActivity(), "");
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    private UIHandler mHandler = new UIHandler(this);

    private static class UIHandler extends Handler {
        private final WeakReference<OrderSubmitFragment> mFragment;

        public UIHandler(OrderSubmitFragment fragment) {
            mFragment = new WeakReference<OrderSubmitFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TAG_BACKPRESS_MESSAGE) {
                OrderSubmitFragment osfer = mFragment.get();
                if (osfer != null)
                    osfer.getActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Result> arg0, Result data) {
        if (data == null || data.mInfo == null) {
            return;
        }

        if (!TextUtils.equals(data.mInfo.getResult(), Tags.RESULT_OK)) {
            Toast.makeText(getActivity(), data.mInfo.getDescription(), Toast.LENGTH_SHORT).show();
            getLoaderManager().destroyLoader(ORDER_SUBMIT_LOADER);
            mHandler.sendEmptyMessage(TAG_BACKPRESS_MESSAGE);
            return;
        }
        
        mAdapter.updateData(data.mInfo.getItems());
      /*  mListFooter.setVisibility(View.VISIBLE);
        mBottom.setVisibility(View.VISIBLE);*/
        
        final String total = data.mInfo.getTotal();
        LogUtil.i(getTag(), "获取订单号");
		getLoaderManager().initLoader(GET_SERVICENUMBER, null, new LoaderCallbacks<GetServiceNumberLoader.Result>(){
			@Override
			public Loader<GetServiceNumberLoader.Result> onCreateLoader(int id, Bundle arg1) {
				if (id == GET_SERVICENUMBER) {
					GetServiceNumberLoader mLoader = new GetServiceNumberLoader(getActivity());
					mLoader.setProgressNotifiable(mLoadingView);
					mLoader.setNeedDatabase(false);
					return (Loader<GetServiceNumberLoader.Result>) mLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(Loader<GetServiceNumberLoader.Result> loader,GetServiceNumberLoader.Result data) {
				if(data != null && data.serviceNumber != null && data.serviceNumber.equalsIgnoreCase("") == false){
					 serviceNumber = data.serviceNumber;
					LogUtil.i(getTag(), "需要处理的订单是："+serviceNumber);
					
					
			        getLoaderManager().initLoader(WIPE_ZERO, 
			    			null, new LoaderCallbacks<XianhuoWipeZeroLoader.Result>() {
			                    BigDecimal oldTotalPrice = BigDecimal.valueOf(Double.valueOf(total));
			                    String mMihomeId = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, "");
								@Override
								public Loader<com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader.Result> onCreateLoader(
										int id, Bundle arg1) {
									if (id == WIPE_ZERO && !TextUtils.isEmpty(mMihomeId)) {
				        	            mLoader = new XianhuoWipeZeroLoader(getActivity(),mMihomeId,oldTotalPrice);
				        	            mLoader.setProgressNotifiable(mLoadingView);
				        	            return (Loader<XianhuoWipeZeroLoader.Result>) mLoader;
				        	        }
				        	        return null;
								}

								@Override
								public void onLoadFinished(
										Loader<com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader.Result> arg0,
										com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader.Result data) {
									BigDecimal newTotalPrice =  oldTotalPrice;
									if(data != null && data.newTotalPrice.doubleValue() > 0){
										newTotalPrice = data.newTotalPrice;
									}
									
									 mCount.setText(String.format(getString(R.string.order_submit_pay,newTotalPrice.doubleValue())));
								        // mCountDescrption.setText(data.info.getAmountDesc());

							        TextView productMoney = (TextView) mListFooter.findViewById(R.id.product_money);
							        productMoney.setText(total);
							        TextView molingMoney = (TextView) mListFooter.findViewById(R.id.moling_money);
							        molingMoney.setText(String.valueOf(newTotalPrice.subtract(BigDecimal.valueOf(Double.parseDouble(total)))));
							        TextView amount = (TextView) mListFooter.findViewById(R.id.amount);
							        amount.setText(String.valueOf(newTotalPrice));
							        mAdapter.updateTitleAndBlack();
							        mListFooter.setVisibility(View.VISIBLE);
							        mBottom.setVisibility(View.VISIBLE);
								}

								@Override
								public void onLoaderReset(
										Loader<com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader.Result> arg0) {
									// TODO Auto-generated method stub
									
								}
			    		
			    	});
				}
				else{
					ToastUtil.show(getActivity(), "获取订单号失败！");
				}
			}

			@Override
			public void onLoaderReset(Loader<GetServiceNumberLoader.Result> arg0) {
			}
		});
        
        
        
       
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {

    }

    public void setData(String jsonData) {
        mJsonData = jsonData;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_ordersubmit);
        getLoaderManager().restartLoader(ORDER_SUBMIT_LOADER, null, this).forceLoad();
    }

    public ShoppingActivity getParent() {
        return (ShoppingActivity) getActivity();
    }

    private void flushVerifyCode() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE);
        activity.startService(intent);
    }

    public void onFetchVcodeCompleted(String action, Intent intent) {
        ShopIntentService.unregisterAction(mFetchDefenseVcodeAction);
        if (Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE.equals(action)) {
            String url = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_URL);
            if (!TextUtils.isEmpty(url)) {
                ImageLoader.getInstance()
                        .loadImage(mVcodeImage, new Image(url), R.drawable.list_default_bg);
            } else {
                ToastUtil.show(getActivity(), R.string.fcode_vcode_fetch_err);
            }
        }
    }

    private OnClickListener mOnButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case BaseAlertDialog.BUTTON_NEGATIVE:
                    break;
                case BaseAlertDialog.BUTTON_POSITIVE:
                    generateOrder(v);
                    break;
                case R.id.submit:
                    if (mNeedCheckCode) {
                        showCheckCodeDialog();
                    } else {
                        generateOrder(v);
                    }
                    break;
                case R.id.change:
                    ShopIntentService.registerAction(mFetchDefenseVcodeAction);
                    mVcodeInput.setText("");
                    Utils.SoftInput.show(getActivity(), mVcodeInput);
                    flushVerifyCode();
                    break;
                default:
                    break;
            }
        }
    };

    public void showCheckCodeDialog() {
        mFetchDefenseVcodeAction = new ShopIntentServiceAction(
                Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE, (Listener) getActivity());
        ShopIntentService.registerAction(mFetchDefenseVcodeAction);
        flushVerifyCode();
        View checkCodeView = LayoutInflater.from(getActivity()).inflate(R.layout.check_code_item,
                null, false);
        mVcodeImage = (ImageView) checkCodeView.findViewById(R.id.vcode_image);
        mVcodeInput = (EditText) checkCodeView.findViewById(R.id.vcode);
        mChangeBtn = (Button) checkCodeView.findViewById(R.id.change);
        mChangeBtn.setOnClickListener(mOnButtonClickListener);
        mCheckCodeDialog = new BaseAlertDialog(getActivity());
        mCheckCodeDialog.setView(checkCodeView);
        mCheckCodeDialog.setTitle(R.string.checkcode_dialog_title);
        mCheckCodeDialog.setPositiveButton(R.string.dialog_ask_ok, mOnButtonClickListener, false);
        mCheckCodeDialog.setNegativeButton(R.string.dialog_ask_cancel, mOnButtonClickListener);
        mCheckCodeDialog.show();
    }

    private void generateOrder(View v) {
		LogUtil.i(getTag(), "处理的订单是："+serviceNumber);
		Intent intent = new Intent(getActivity(), ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_ORDER_SUBMIT);
        intent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON, mJsonData);
        intent.putExtra(Constants.serviceNumber, serviceNumber);
        if (mNeedCheckCode) {
            String code = mVcodeInput.getText().toString();
            if (TextUtils.isEmpty(code)) {
                ToastUtil.show(getActivity(), R.string.checkcode_not_empty);
                Utils.SoftInput.show(getActivity(), mVcodeInput);
                return;
            } else {
                if (mCheckCodeDialog != null) {
                    mCheckCodeDialog.dismiss();
                }
                intent.putExtra(Constants.Intent.EXTRA_CHECKCODE_VCODE, code);
                mProgressDialog = ProgressDialog.show(getActivity(), null,
                        getResources().getString(R.string.fcode_waiting), false, true);
            }
        }
        getActivity().startService(intent);
        setButtonState((Button) v, false, getString(R.string.order_submit_button_submit_n));
    }

    public void onSubmitCallback(Intent intent) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (!mNeedCheckCode) {
            setButtonState(mSubmit, true, getString(R.string.order_submit_button_submit));
        }
    }

    public void setButtonState(Button button, boolean isClick, String text) {
        button.setEnabled(isClick);
        button.setText(text);
    }

}
