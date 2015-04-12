package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.AttentionTestActivity;
import com.xiaomi.xms.sales.util.Constants;

public class AttentionTestFragment extends BaseFragment {

	private Button mNumButton;
	private Button mPicButton;
	private Button mWordButton;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mNumButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_NUMBER_TEST_ACTION);
				getActivity().startActivity(intent);

			}
		});

		mPicButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_PICTURE_TEST_ACTION);
				getActivity().startActivity(intent);
			}
		});

		mWordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), AttentionTestActivity.class);
				intent.setAction(Constants.Intent.ACTION_ATTENTION_WORD_TEST_ACTION);
				getActivity().startActivity(intent);

			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.attention_test_fragment,
				container, false);
		mNumButton = (Button) view.findViewById(R.id.numbertest);
		mPicButton = (Button) view.findViewById(R.id.picturetest);
		mWordButton = (Button) view.findViewById(R.id.wordtest);
		return view;
	}

}
