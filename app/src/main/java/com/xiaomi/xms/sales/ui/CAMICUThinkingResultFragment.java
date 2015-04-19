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
import com.xiaomi.xms.sales.activity.CAMICUThinkingTestActivity;
import com.xiaomi.xms.sales.activity.ICUMainActivity;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CAMICUThinkingResultFragment extends BaseFragment {

    Button next_patient_btn = null;
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


    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camicu_thinking_result_fragment, container, false);
        next_patient_btn = (Button) view.findViewById(R.id.attention_next_patient);

        if(!Utils.Preference.getBooleanPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT_BOOLEAN, false)){
            TextView res_title = (TextView) view.findViewById(R.id.rass_res_title);
            res_title.setText("病人的意识与基线一致：特征1  -" );
            TextView res_content = (TextView) view.findViewById(R.id.rass_res_content);
            res_content.setText("无谵妄发生" );
            next_patient_btn.setVisibility(View.INVISIBLE);
        }
		return view;
	}
	

}
