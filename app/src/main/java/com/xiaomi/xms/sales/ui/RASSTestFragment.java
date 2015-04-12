package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderViewProductSnAdapter;
import com.xiaomi.xms.sales.loader.RefundConfirmLoader;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.ui.RASSTestFragment.RadioButtonInfo.Tag;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class RASSTestFragment extends BaseFragment implements
		OnCheckedChangeListener {

	private static final String TAG = "ReturnChoiceFragment";
	private static final int ORDER_INFO_LOADER = 0;
	private static final int GROUP_RETURN_CHOICE = 1;
	private static final int REFUND_CONFIRM_LOADER = 2;
	private Button mConfirmButton;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.return_choice_fragment,
				container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);

		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer,
				null, false);
		mListFooterView.setVisibility(View.VISIBLE);
		mListRadioButtonFooterView = inflater.inflate(
				R.layout.return_choice_radiobutton_footer, null, false);
		mListRadioButtonFooterView.setVisibility(View.VISIBLE);
		
		mListView.addFooterView(mListRadioButtonFooterView);
		mListView.addFooterView(mListFooterView);
		mListView
				.setPadding(
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						0,
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding));
		mActionContainer = view.findViewById(R.id.action_container);
		mradioGroupContainer = mListRadioButtonFooterView
				.findViewById(R.id.return_choice_radiogroup);

		mConfirmButton = (Button) view.findViewById(R.id.rass_confirm_btn);
		mReturnChoiceRadioGroup = (RadioGroup) mListRadioButtonFooterView
				.findViewById(R.id.return_goods_status_choice);
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
			mOrderId = mBundle
					.getString(Constants.Intent.EXTRA_RETURN_ORDER_SN_STR);
			mOrderStatus = mBundle
					.getString(Constants.Intent.EXTRA_RETURN_ORDER_STATUS_TYPE);
			if (mOrderStatus == null) {
				mOrderStatus = "";
			}
		} else {
			mBundle = new Bundle();
		}
		LogUtil.d(TAG, mOrderId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new OrderViewProductSnAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//				setSumbitButtonAttribute(false, R.string.return_sending);
				RadioButton checkRadioButton = (RadioButton) mReturnChoiceRadioGroup.findViewById(mReturnChoiceRadioGroup.getCheckedRadioButtonId());
				if(checkRadioButton == null){
					ToastUtil.show(getActivity(), "请至少选择一项");
					return;
				}
				String rassResult = checkRadioButton.getTag().toString();
				int rassResultInt = Integer.parseInt(rassResult);
				if( rassResultInt< -3){
					ToastUtil.show(getActivity(), "无需再评估，请稍后再作评估，同时储存结束");
				}else{
					ToastUtil.show(getActivity(), "提交成功，您的分数为："+rassResult+"请滑动屏幕进行注意力测试");
				}
				Utils.Preference.setStringPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT, rassResult);
				if(rassResultInt == 4){
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN, false);
				}else{
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN, true);
				}
				
//				SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
//				father.showFragment(
//						SameDayReturnActivity.Fragments.TAG_RETURN_CONFIRM_FRAGMENT,
//						mBundle, true);
			}
		});

		// order id
		// invoice info
		// add time
		mActionContainer.setVisibility(View.VISIBLE);
		mradioGroupContainer.setVisibility(View.VISIBLE);

	}

	private void returnAsynInfo() {
		getLoaderManager().restartLoader(REFUND_CONFIRM_LOADER, null,
				new LoaderCallbacks<RefundConfirmLoader.Result>() {

					@Override
					public RefundConfirmLoader onCreateLoader(int id,
							Bundle arg1) {
						if (id == REFUND_CONFIRM_LOADER) {
							String isGood = Utils.Preference
									.getStringPref(
											getActivity(),
											Constants.SameDayReturn.PREF_KEY_CHOICE_RESULT,
											"1");
							String mOrderIdStr = Utils.Preference
									.getStringPref(
											getActivity(),
											Constants.SameDayReturn.PREF_KEY_SERVICENUMBER,
											"");
							RefundConfirmLoader refundConfirmLoader = new RefundConfirmLoader(
									getActivity(), mOrderIdStr, isGood);
							refundConfirmLoader.setNeedDatabase(false);
							refundConfirmLoader
									.setProgressNotifiable(mLoadingView);
							return refundConfirmLoader;
						}
						return null;
					}

					@Override
					public void onLoadFinished(
							Loader<RefundConfirmLoader.Result> loader,
							RefundConfirmLoader.Result data) {
						if (data != null && data.responseInfo != null
								&& data.responseInfo.equalsIgnoreCase("OK")) {
							ToastUtil.show(getActivity(), "消费撤销同步信息成功");
							if (getActivity() != null) {
								getActivity().finish();
							}
						} else {
							ToastUtil.show(getActivity(), data.responseInfo);
							setSumbitButtonAttribute(true,
									R.string.return_resend);
						}

					}

					@Override
					public void onLoaderReset(
							Loader<RefundConfirmLoader.Result> arg0) {

					}
				});
	}

	private void setSumbitButtonAttribute(boolean isClick, int resId) {
		mConfirmButton.setEnabled(isClick);
		mConfirmButton.setText(resId);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton button = (RadioButton) group.findViewById(checkedId);

		if (group == mReturnChoiceRadioGroup) {
            if (button.getTag().toString().equals("11") ) {
            	if(group.getChildCount() == 6){
            		firstNoneofAboveinitRadioGroup();
            	}else{
            		secondNoneofAboveinitRadioGroup();
            	}
            	
            }
			if (mReturnChoiceButton != null) {
				mReturnChoiceButton
						.setBackgroundResource(R.drawable.radiobutton_bottom_bg);
			}

			setDefaultValue(GROUP_RETURN_CHOICE, button.getTag().toString());
		}
	}

	private String shortName(String str){
		if(str.contains("-")){
			return str.substring(0, str.indexOf("-"));
		}else{
			return str;
		}
		
	}
	
	private void initRadioGroup() {
		ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();

		RadioButtonInfo button1 = new RadioButtonInfo();
		button1.mTitle = shortName(getString(R.string.rass_test_1));
		button1.mTotalTitle = getString(R.string.rass_test_1);
		button1.mTag = Tags.CheckoutSubmit.rass_test_1;
		buttons.add(button1);

		RadioButtonInfo button2 = new RadioButtonInfo();
		button2.mTitle = shortName(getString(R.string.rass_test_2));
		button2.mTotalTitle = getString(R.string.rass_test_2);
		button2.mTag = Tags.CheckoutSubmit.rass_test_2;
		buttons.add(button2);

		RadioButtonInfo button3 = new RadioButtonInfo();
		button3.mTitle = shortName(getString(R.string.rass_test_3));
		button3.mTotalTitle = getString(R.string.rass_test_3);
		button3.mTag = Tags.CheckoutSubmit.rass_test_3;
		buttons.add(button3);

		RadioButtonInfo button4 = new RadioButtonInfo();
		button4.mTitle = shortName(getString(R.string.rass_test_4));
		button4.mTotalTitle = getString(R.string.rass_test_4);
		button4.mTag = Tags.CheckoutSubmit.rass_test_4;
		buttons.add(button4);

		RadioButtonInfo button5 = new RadioButtonInfo();
		button5.mTitle = shortName(getString(R.string.rass_test_5));
		button5.mTotalTitle = getString(R.string.rass_test_5);
		button5.mTag = Tags.CheckoutSubmit.rass_test_5;
		buttons.add(button5);

		RadioButtonInfo button11 = new RadioButtonInfo();
		button11.mTitle = shortName(getString(R.string.rass_test_11));
		button11.mTotalTitle = getString(R.string.rass_test_11);
		button11.mTag = Tags.CheckoutSubmit.rass_test_11;
		buttons.add(button11);

		addRadioButtons(mReturnChoiceRadioGroup, buttons,
				getDefaultValue(GROUP_RETURN_CHOICE));
	}
	
	private void firstNoneofAboveinitRadioGroup() {
		mReturnChoiceRadioGroup.removeViewAt(mReturnChoiceRadioGroup.getChildCount()-1);
		ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();
		
		RadioButtonInfo button6 = new RadioButtonInfo();
		button6.mTitle = shortName(getString(R.string.rass_test_6));
		button6.mTotalTitle = getString(R.string.rass_test_6);
		button6.mTag = Tags.CheckoutSubmit.rass_test_6;
		buttons.add(button6);
		
		RadioButtonInfo button7 = new RadioButtonInfo();
		button7.mTitle = shortName(getString(R.string.rass_test_7));
		button7.mTotalTitle = getString(R.string.rass_test_7);
		button7.mTag = Tags.CheckoutSubmit.rass_test_7;
		buttons.add(button7);
		
		RadioButtonInfo button8 = new RadioButtonInfo();
		button8.mTitle = shortName(getString(R.string.rass_test_8));
		button8.mTotalTitle = getString(R.string.rass_test_8);
		button8.mTag = Tags.CheckoutSubmit.rass_test_8;
		buttons.add(button8);

		RadioButtonInfo button11 = new RadioButtonInfo();
		button11.mTitle = shortName(getString(R.string.rass_test_11));
		button11.mTotalTitle = getString(R.string.rass_test_11);
		button11.mTag = Tags.CheckoutSubmit.rass_test_11;
		buttons.add(button11);

		addRadioButtons(mReturnChoiceRadioGroup, buttons,
				getDefaultValue(GROUP_RETURN_CHOICE));
	}
	
	private void secondNoneofAboveinitRadioGroup() {
		mReturnChoiceRadioGroup.removeViewAt(mReturnChoiceRadioGroup.getChildCount()-1);
		ArrayList<RadioButtonInfo> buttons = new ArrayList<RadioButtonInfo>();
		
		RadioButtonInfo button9 = new RadioButtonInfo();
		button9.mTitle = shortName(getString(R.string.rass_test_9));
		button9.mTotalTitle = getString(R.string.rass_test_9);
		button9.mTag = Tags.CheckoutSubmit.rass_test_9;
		buttons.add(button9);

		RadioButtonInfo button10 = new RadioButtonInfo();
		button10.mTitle = shortName(getString(R.string.rass_test_10));
		button10.mTotalTitle = getString(R.string.rass_test_10);
		button10.mTag = Tags.CheckoutSubmit.rass_test_10;
		buttons.add(button10);

		addRadioButtons(mReturnChoiceRadioGroup, buttons,
				getDefaultValue(GROUP_RETURN_CHOICE));
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
		public String mTotalTitle;
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

	private void addRadioButtons(RadioGroup group,
			ArrayList<RadioButtonInfo> items, String defaultValue) {
		LogUtil.d(TAG, "addRadioButtons");
		String value = null;
		RadioButton first = null;
		int count = items.size();
		for (int i = 0; i < count; i++) {
			final RadioButtonInfo item = items.get(i);
			final RadioButton button = new RadioButton(getActivity());
			button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {  

	            @Override 

	            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
	            	if(isChecked){
	            		button.setText(item.mTotalTitle);
	            	}else{
	            		button.setText(shortName(item.mTotalTitle));
	            	}
	            }  

	        }); 
			button.setTextSize(23);
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
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
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
