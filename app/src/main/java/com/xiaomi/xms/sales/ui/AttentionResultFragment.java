package com.xiaomi.xms.sales.ui;

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
import com.xiaomi.xms.sales.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttentionResultFragment extends BaseFragment {

    Button next_patient_btn = null;
    Button next_thinking_btn = null;
    TextView res_title = null;
    TextView res_content = null;
    ArrayList<String> listStr = null;  
    private List<HashMap<String, Object>> list = null;  
	private Bundle mBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        next_patient_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ICUMainActivity.class);
                startActivity(intent);
            }
        });

        next_thinking_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CAMICUThinkingTestActivity.class);
                getActivity().startActivity(intent);
            }
        });

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attention_result_fragment, container, false);
        next_patient_btn = (Button) view.findViewById(R.id.attention_next_patient);
        next_thinking_btn = (Button) view.findViewById(R.id.attention_next_thinking);
        res_title = (TextView) view.findViewById(R.id.rass_res_title);
        res_content = (TextView) view.findViewById(R.id.rass_res_content);
        if(!Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT_BOOLEAN, false)){
            res_title.setText("   病人错误  <3  次：  特征3  —" );
            res_content.setText("特征1＋\n" + "特征2 — \n" +"特征3 —   " +"无谵妄发生");
            next_thinking_btn.setVisibility(View.INVISIBLE);
        } else if(!Utils.Preference.getStringPref(getActivity(), Constants.IcuGradeResult.RASS_TEMP_RESULT,"0").equals("4")) {
            res_title.setText("   病人错误  >=3  次：  特征2  +" );
            res_content.setText("特征1＋\n" + "特征2+  \n" +"特征3 +   " +"谵妄");
            next_thinking_btn.setVisibility(View.INVISIBLE);
        } else {
            res_title.setText("   病人错误  >=3  次：  特征2  +" );
            res_content.setText("特征1＋\n" + "特征2+  \n" +"特征3 -");
            next_patient_btn.setVisibility(View.INVISIBLE);
        }

		return view;
	}
	

}
