package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.SameDayReturnActivity;
import com.xiaomi.xms.sales.adapter.OrderViewProductSnAdapter;
import com.xiaomi.xms.sales.loader.RefundConfirmLoader;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.ui.ReturnChoiceFragment.RadioButtonInfo.Tag;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class ReturnChoiceFragment extends BaseFragment implements OnCheckedChangeListener {

	private static final String TAG = "ReturnChoiceFragment";
	private static final int ORDER_INFO_LOADER = 0;
	private static final int GROUP_RETURN_CHOICE = 1;
	private static final int REFUND_CONFIRM_LOADER = 2;
	private Button mPayButton;
	private BaseListView mListView;
	private View mListFooterView;
	private View mListRadioButtonFooterView;
	private View mActionContainer;
	private View mradioGroupContainer;
	private OrderViewProductSnAdapter mAdapter;
	private EmptyLoadingView mLoadingView;
	private RadioGroup mReturnChoiceRadioGroup;
	private RadioButton mReturnChoiceButton;
	private String mOrderId;
	private String mOrderStatus;
	private OnOrderStatusChangedListener mOrderStatusListener;
	private HashMap<Integer, String> mFormDefaultValue = new HashMap<Integer, String>();
	private Bundle mBundle;

	public interface OnOrderStatusChangedListener {
		public void onOrderStatusChanged();
	}

	public void setOrderStatusListener(OnOrderStatusChangedListener listener) {
		mOrderStatusListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.return_choice_fragment, container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer, null, false);
		mListFooterView.setVisibility(View.VISIBLE);
		mListRadioButtonFooterView = inflater.inflate(R.layout.return_choice_radiobutton_footer, null, false);
		mListRadioButtonFooterView.setVisibility(View.VISIBLE);
		mListView.addFooterView(mListFooterView);
		mListView.addFooterView(mListRadioButtonFooterView);
		mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0, getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding), getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding));
		mActionContainer = view.findViewById(R.id.action_container);
		mradioGroupContainer = mListRadioButtonFooterView.findViewById(R.id.return_choice_radiogroup);
		
		mPayButton = (Button) view.findViewById(R.id.choice_confirm_btn);
		mReturnChoiceRadioGroup = (RadioGroup) mListRadioButtonFooterView.findViewById(R.id.return_goods_status_choice);
		mReturnChoiceRadioGroup.setOnCheckedChangeListener(this);
		handleIntent();

		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.order_list_empty);
		initRadioGroup();
		return view;
	}

	private void handleIntent() {
		mBundle = getArguments();
		if (mBundle != null) {
			mOrderId = mBundle.getString(Constants.Intent.EXTRA_RETURN_ORDER_SN_STR);
			mOrderStatus = mBundle.getString(Constants.Intent.EXTRA_RETURN_ORDER_STATUS_TYPE);
			if(mOrderStatus == null){
				mOrderStatus = "";
			}
		}else{
			mBundle = new Bundle();
		}
		LogUtil.d(TAG, mOrderId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new OrderViewProductSnAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mPayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setSumbitButtonAttribute(false,R.string.return_sending);
				if(mOrderStatus.equals("REPEAL")){
					returnAsynInfo();
					return;
				}
				SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
				father.showFragment(SameDayReturnActivity.Fragments.TAG_RETURN_CONFIRM_FRAGMENT, mBundle, true);
			}
		});
		
		ArrayList<ProductBrief> productSnList = getProductSnList();
		
		mAdapter.updateData(productSnList);
          // order id
          // invoice info
          // add time
        mActionContainer.setVisibility(View.VISIBLE);
        mradioGroupContainer.setVisibility(View.VISIBLE);

	}

	private void returnAsynInfo() {
		getLoaderManager().restartLoader(REFUND_CONFIRM_LOADER, null, new LoaderCallbacks<RefundConfirmLoader.Result>() {

			@Override
			public RefundConfirmLoader onCreateLoader(int id, Bundle arg1) {
				if (id == REFUND_CONFIRM_LOADER) {
					String isGood = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_CHOICE_RESULT, "1");
					String mOrderIdStr = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_SERVICENUMBER, "");
					RefundConfirmLoader refundConfirmLoader = new RefundConfirmLoader(getActivity(), mOrderIdStr, isGood);
					refundConfirmLoader.setNeedDatabase(false);
					refundConfirmLoader.setProgressNotifiable(mLoadingView);
					return refundConfirmLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(Loader<RefundConfirmLoader.Result> loader, RefundConfirmLoader.Result data) {
				if (data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
					ToastUtil.show(getActivity(), "消费撤销同步信息成功");
					if (getActivity() != null) {
						getActivity().finish();
					}
				}else{
					ToastUtil.show(getActivity(), data.responseInfo);
					setSumbitButtonAttribute(true,R.string.return_resend);
				}

			}

			@Override
			public void onLoaderReset(Loader<RefundConfirmLoader.Result> arg0) {

			}
		});
	}
	
	private void setSumbitButtonAttribute(boolean isClick, int resId) {
		mPayButton.setEnabled(isClick);
		mPayButton.setText(resId);
	}
	
	private ArrayList<ProductBrief> getProductSnList(){
		ArrayList<ProductBrief> productSnObject = null;
		Map<String, ProductInfo> mProductInfoList = new HashMap<String, ProductInfo>();
		SharedPreferences pmySharedPreferences = getActivity().getSharedPreferences(Constants.productSnCache, Activity.MODE_PRIVATE);
		if(pmySharedPreferences != null){
			String productSnList = null;
			if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
				productSnList = pmySharedPreferences.getString("productSnList", null);
			}
			if(productSnList != null){
					String pproductBase64 = productSnList;
					if (pproductBase64 != null && pproductBase64.length() > 0) {
						// 对Base64格式的字符串进行解码
						byte[] base64Bytes = Base64.decode(pproductBase64.getBytes(), Base64.DEFAULT);
						ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
						ObjectInputStream ois = null;
						try {
							ois = new ObjectInputStream(bais);
						} catch (StreamCorruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 从ObjectInputStream中读取Product对象
						try {
							productSnObject = (ArrayList<ProductBrief>) ois.readObject();
						} catch (OptionalDataException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
			}
		}
		return productSnObject;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton button = (RadioButton) group.findViewById(checkedId);
		if (group == mReturnChoiceRadioGroup) {
//			if (TextUtils.equals(button.getTag().toString(), Tags.CheckoutSubmit.RETURN_CHOICE_GOOD)) {
//				mReturnChoiceButton = button;
//				button.setBackgroundResource(R.drawable.radiobottom_middle_invoice_p);
//			} else {
//				if (mReturnChoiceButton != null) {
//					mReturnChoiceButton.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
//				}
//			}
			setDefaultValue(GROUP_RETURN_CHOICE, button.getTag().toString());
			Utils.Preference.setStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_CHOICE_RESULT, getDefaultValue(GROUP_RETURN_CHOICE));
		}
	}
	
	private void initRadioGroup() {
		ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();

//		RadioButtonInfo button2 = new RadioButtonInfo();
//		button2.mTitle = getString(R.string.return_choice_good);
//		button2.mTag = Tags.CheckoutSubmit.RETURN_CHOICE_GOOD;
//		buttons.add(button2);
//
//		RadioButtonInfo button3 = new RadioButtonInfo();
//		button3.mTitle = getString(R.string.return_choice_bad);
//		button3.mTag = Tags.CheckoutSubmit.RETURN_CHOICE_BAD;
//		buttons.add(button3);

		addRadioButtons(mReturnChoiceRadioGroup, buttons, getDefaultValue(GROUP_RETURN_CHOICE));
	}
	
	private String getDefaultValue(int key) {
		String ret = mFormDefaultValue.get(key);
		LogUtil.d(TAG, "getDefaultValue: key is " + key + ", value is " + ret);
		return ret;
	}

	private void setDefaultValue(int key, String value) {
		LogUtil.d(TAG, "SetDefaultValue: key is " + key + ", value is " + value);
		mFormDefaultValue.put(key, value);
	}
	
	class RadioButtonInfo {
		public String mTitle;
		public String mTag;
		public ArrayList<Tag> mTags = new ArrayList<Tag>();

		public void addTag(int key, String value) {
			Tag tag = new Tag();
			tag.key = key;
			tag.value = value;
			mTags.add(tag);
		}

		class Tag {
			public int key;
			public String value;
		}
	}
	
	private void addRadioButtons(RadioGroup group, ArrayList<RadioButtonInfo> items, String defaultValue) {
		LogUtil.d(TAG, "addRadioButtons");
		String value = null;
		RadioButton first = null;
		int count = items.size();
		for (int i = 0; i < count; i++) {
			RadioButtonInfo item = items.get(i);
			RadioButton button = new RadioButton(getActivity());
			if (i == 0) {
				first = button;
			}
			button.setText(item.mTitle);
			if (item.mTags.size() == 0) {
				button.setTag(item.mTag);
			} else {
				for (int j = 0; j < item.mTags.size(); j++) {
					Tag tag = item.mTags.get(j);
					button.setTag(tag.key, tag.value);
				}
			}
			group.addView(button);

			if (count == 1) {
				button.setBackgroundResource(R.drawable.radiobutton_single_bg);
			} else if (i == 0) {
				button.setBackgroundResource(R.drawable.radiobutton_up_bg);
			} else if (i == count - 1) {
				button.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
			} else {
				button.setBackgroundResource(R.drawable.radiobutton_middle_bg);
			}
			button.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			button.setLayoutParams(params);
			value = button.getTag().toString();
			if (defaultValue == null) {
				first.setChecked(true);
			} else if (TextUtils.equals(value, defaultValue)) {
				button.setChecked(true);
			}
		}
	}
	
	
}
