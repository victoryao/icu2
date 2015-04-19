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
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.CAMICUAttentionTestActivity;
import com.xiaomi.xms.sales.activity.CAMICUThinkingTestActivity;
import com.xiaomi.xms.sales.activity.ICUMainActivity;
import com.xiaomi.xms.sales.adapter.ICURadioAdapter;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;

public class CamICURASSResultFragment extends BaseFragment {

    Button next_patient_btn = null;
    Button next_attention_btn = null;
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
        next_patient_btn = (Button) view.findViewById(R.id.rass_next_patient);
        next_attention_btn = (Button) view.findViewById(R.id.rass_next_attention);
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}

        next_patient_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ICUMainActivity.class);
                startActivity(intent);
            }
        });

        next_attention_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CAMICUAttentionTestActivity.class);
                getActivity().startActivity(intent);
            }
        });

        if(!Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN,true) && !Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_STEP3,false)){
            TextView res_title = (TextView) view.findViewById(R.id.rass_res_title);
            res_title.setText("病人的意识与基线一致：特征1  -" );
            TextView res_content = (TextView) view.findViewById(R.id.rass_res_content);
            res_content.setText("无谵妄发生" );
            next_patient_btn.setVisibility(View.INVISIBLE);
        }
        if(Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT_BOOLEAN,true)) {
            TextView res_title = (TextView) view.findViewById(R.id.rass_res_title);
            res_title.setText("病人的意识与基线不一致：特征1  +" );
            next_patient_btn.setVisibility(View.INVISIBLE);
            TextView res_content = (TextView) view.findViewById(R.id.rass_res_content);
            if(Utils.Preference.getStringPref(getActivity(), Constants.IcuGradeResult.RASS_TEMP_RESULT,"0").equals("4")) {
                res_content.setText("特征1＋\n特征3 —" );
            } else {
                res_content.setText("特征1＋\n特征3 +" );
            }
        }
        //第一步选择底项&&选择过第三步
        if(Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.RASS_STEP3,false)&&Utils.Preference.getStringPref(getActivity(), Constants.IcuGradeResult.RASS_TEMP_RESULT,"0").equals("5")){
            TextView res_content = (TextView) view.findViewById(R.id.rass_res_content);
            res_content.setText("暂时无法评估" );
            next_attention_btn.setVisibility(View.INVISIBLE);
        }
        String temp_result=Utils.Preference.getStringPref(getActivity(), Constants.IcuGradeResult.RASS_TEMP_RESULT,"0");
        Utils.Preference.setStringPref(getActivity(), Constants.IcuGradeResult.RASS_RESULT, temp_result);
   /*
		next_btn = (Button) view.findViewById(R.id.rass_next_patient);
//        next_btn.setVisibility(View.);
		next_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), CAMICUThinkingTestActivity.class);
//				Intent intent = new Intent(getActivity(), CAMICUAttentionTestActivity.class);
				getActivity().startActivity(intent);
			}
		});
        **/
		return view;
	}
	

}
