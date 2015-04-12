package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.CAMICUMainTestActivity;

public class CamICUTestBeginFragment extends BaseFragment {

	private ImageView RassTestBeginbtn;

	private Bundle mBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cam_icu_test_begin_fragment, container, false);
		RassTestBeginbtn = (ImageView) view.findViewById(R.id.RASSTestBegin);
		RassTestBeginbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CAMICUMainTestActivity father = (CAMICUMainTestActivity)getActivity();
				father.showFragment(CAMICUMainTestActivity.Fragments.TAG_CAM_ICU_RASS_STEP1_FRAGMENT, mBundle, true);
				
			}
		});
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}
		
		return view;
	}



}
