package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.CAMICUAttentionTestActivity;
import com.xiaomi.xms.sales.activity.CAMICUThinkingTestActivity;
import com.xiaomi.xms.sales.adapter.ICURadioAdapter;
import com.xiaomi.xms.sales.util.Constants;

public class CamICURASSResultFragment extends BaseFragment {

    Button next_btn = null;  
    
    ArrayList<String> listStr = null;  
    private List<HashMap<String, Object>> list = null;  
    private ICURadioAdapter adapter;  
	private Bundle mBundle;
	
	public void init(){
		if(isAdded()) {  
			   
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cam_icu_rass_result_fragment, container, false);
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}
		next_btn = (Button) view.findViewById(R.id.next_btn);  
		next_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), CAMICUThinkingTestActivity.class);
//				Intent intent = new Intent(getActivity(), CAMICUAttentionTestActivity.class);
				getActivity().startActivity(intent);
			}
		});
		return view;
	}
	

}
