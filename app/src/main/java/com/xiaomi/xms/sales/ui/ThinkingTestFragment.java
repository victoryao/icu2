package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderViewProductSnAdapter;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class ThinkingTestFragment extends BaseFragment {
	private Button mConfirmButton;
	private TextView mTestingResult;
	private RadioGroup mRadioGroup;
	private BaseListView mListView;
	private View mListFooterView;
	private View thinkingTestView;
	private OrderViewProductSnAdapter mAdapter;
	private EmptyLoadingView mLoadingView;
	private CheckBox mThinkingCheckedBox1;
	private CheckBox mThinkingCheckedBox2;
	private CheckBox mThinkingCheckedBox3;
	private CheckBox mThinkingCheckedBox4;
	private CheckBox mThinkingCheckedBox5;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mAdapter = new OrderViewProductSnAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
				int count = 0;
				if(mThinkingCheckedBox1.isChecked()){
					count++;
				}
				if(mThinkingCheckedBox2.isChecked()){
					count++;
				}
				if(mThinkingCheckedBox3.isChecked()){
					count++;
				}
				if(mThinkingCheckedBox4.isChecked()){
					count++;
				}
				if(mThinkingCheckedBox5.isChecked()){
					count++;
				}
				String res = "";
				if( count > 1){
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.THINK_RESULT_BOOLEAN, true);
				}else{
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.THINK_RESULT_BOOLEAN, false);
				}
				if (Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN, false) && 
						Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT_BOOLEAN, false)) {
					if ( Integer.parseInt(Utils.Preference.getStringPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT, "0")) !=0 ||
							count > 1) {
						res = "本病人谵妄阳性";
						ToastUtil.show(getActivity(), "本病人谵妄阳性");
					} else {
						res = "本病人谵妄阴性";
						ToastUtil.show(getActivity(), "本病人谵妄阴性");
					}
				} else {
					res = "本病人谵妄阴性";
					ToastUtil.show(getActivity(), "本病人谵妄阴性");
				}
				mTestingResult.setText(res);
				//ToastUtil.show(getActivity(), "提交成功，请进行思维测试");
				Utils.Preference.setStringPref(getActivity(), Constants.IcuGradeResult.THINK_RESULT, count+"");
				
//				SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
//				father.showFragment(
//						SameDayReturnActivity.Fragments.TAG_RETURN_CONFIRM_FRAGMENT,
//						mBundle, true);
			}
		});

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.thinking_cotainer_fragment,
				container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		view.findViewById(R.id.rass_confirm_btn);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mConfirmButton = (Button)view.findViewById(R.id.thinking_confirm_btn);
		mTestingResult = (TextView)view.findViewById(R.id.testing_result);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer,
				null, false);
		thinkingTestView = inflater.inflate(R.layout.thinking_test_fragment, null,
				false);
		mListFooterView.setVisibility(View.VISIBLE);
		mListView.addFooterView(thinkingTestView);
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
		
//		mConfirmButton = (Button) thinkingTestView.findViewById(R.id.confirm_btn);
		mThinkingCheckedBox1 = (CheckBox) thinkingTestView.findViewById(R.id.thinking_test_n1);
		mThinkingCheckedBox2 = (CheckBox) thinkingTestView.findViewById(R.id.thinking_test_n2);
		mThinkingCheckedBox3 = (CheckBox) thinkingTestView.findViewById(R.id.thinking_test_n3);
		mThinkingCheckedBox4 = (CheckBox) thinkingTestView.findViewById(R.id.thinking_test_n4);
		mThinkingCheckedBox5 = (CheckBox) thinkingTestView.findViewById(R.id.thinking_test_n5);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.order_list_empty);
		return view;
	}

}
