package com.xiaomi.xms.sales.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.WordTestAdapter;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

public class AttentionWordTestFragment extends BaseFragment {
	
	private Button mConfirmButton;
	private RadioGroup mRadioGroup;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//				setSumbitButtonAttribute(false, R.string.return_sending);
				RadioButton checkRadioButton = (RadioButton) mRadioGroup.findViewById(mRadioGroup.getCheckedRadioButtonId());
				if(checkRadioButton == null){
					ToastUtil.show(getActivity(), "请至少选择一项");
					return;
				}
				
				String attentionResult = checkRadioButton.getTag().toString();
				int rassResultInt = Integer.parseInt(attentionResult);
				if( rassResultInt == 2){
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN, true);
				}else{
					Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN, false);
				}
				ToastUtil.show(getActivity(), "提交成功，请滑动屏幕进行思维测试");
				Utils.Preference.setStringPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT, attentionResult);
				getActivity().finish();
				
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
		View view = inflater.inflate(R.layout.word_test_fragment, container,
				false);
		mConfirmButton = (Button) view.findViewById(R.id.confirm_btn);
		mRadioGroup = (RadioGroup) view.findViewById(R.id.word_test_summary_choice);
		// 取得GridView对象
		GridView gridview = (GridView) view.findViewById(R.id.gridview);
		// 添加元素给gridview
		gridview.setAdapter(new WordTestAdapter(getActivity()));

		// 事件监听
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tv = (TextView)v;
				if(tv.getCurrentTextColor() == Color.GRAY){
					if(tv.getText().equals("A")){
						tv.setTextColor(Color.RED);
					}else{
						tv.setTextColor(Color.BLACK);
					}
				}else{
					tv.setTextColor(Color.GRAY);
				}
			}

		});
		return view;
	}

}
