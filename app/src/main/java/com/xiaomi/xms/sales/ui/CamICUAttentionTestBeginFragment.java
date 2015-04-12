package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.AttentionTestActivity;
import com.xiaomi.xms.sales.activity.CAMICUMainTestActivity;
import com.xiaomi.xms.sales.util.Constants;

public class CamICUAttentionTestBeginFragment extends BaseFragment {

	private ImageView RassTestBeginbtn;

	private Bundle mBundle;
	
	private View numView;
	
	private View picView;
	
	private View wordView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cam_icu_attention_test_fragment, container, false);
		RassTestBeginbtn = (ImageView) view.findViewById(R.id.RASSTestBegin);
		numView = view.findViewById(R.id.icu_num_test);
		picView = view.findViewById(R.id.icu_pic_test);
		wordView = view.findViewById(R.id.icu_word_test);
		
		numView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_NUMBER_TEST_ACTION);
				getActivity().startActivity(intent);

			}
		});

		picView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_PICTURE_TEST_ACTION);
				getActivity().startActivity(intent);
			}
		});

		wordView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_WORD_TEST_ACTION);
				getActivity().startActivity(intent);

			}
		});
		
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}
		
		return view;
	}



}
