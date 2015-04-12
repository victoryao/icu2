package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.CAMICUMainTestActivity;
import com.xiaomi.xms.sales.adapter.ICURadioAdapter;
import com.xiaomi.xms.sales.adapter.ViewHolder;

public class CamICURASSStep3Fragment extends BaseFragment {

	TextView tv = null;  
    ListView lv = null;  
    Button btn_selectAll = null;  
    Button btn_inverseSelect = null;  
    Button btn_calcel = null;  
    
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
		View view = inflater.inflate(R.layout.cam_icu_rass_step3_fragment, container, false);
		mBundle = getArguments();
		if(mBundle == null){
			mBundle = new Bundle();
		}
		tv = (TextView) view.findViewById(R.id.tv);  
        lv = (ListView) view.findViewById(R.id.lv);  
        showCheckBoxListView();  
          
		return view;
	}
	
	// 显示带有checkbox的listview  
    public void showCheckBoxListView() {  
    	final String name[] = {
        		shortName(this.getResources().getString(R.string.rass_test_11)),
        		shortName(this.getResources().getString(R.string.rass_test_12))
        		};  
    	
        list = new ArrayList<HashMap<String, Object>>();  
        for (int i = 0; i < name.length; i++) {  
            HashMap<String, Object> map = new HashMap<String, Object>();  
            map.put("item_tv", name[i]);  
            map.put("item_cb", false);  
            list.add(map);  
  
            adapter = new ICURadioAdapter(getActivity(), list, R.layout.icu_radio_list_viewitem,  
            		new String[] { "item_tv", "item_cb" },
            		new int[] {R.id.item_tv, R.id.item_cb });
            		
            lv.setAdapter(adapter);  
            listStr = new ArrayList<String>();  
            lv.setOnItemClickListener(new OnItemClickListener() {  
  
                @Override  
                public void onItemClick(AdapterView<?> arg0, View view,  
                        int position, long arg3) {  
                    ViewHolder holder = (ViewHolder) view.getTag();  
                    holder.cb.toggle();// 在每次获取点击的item时改变checkbox的状态  
//                    adapter.isSelected.put(position, holder.cb.isChecked()); // 同时修改map的值保存状态 
//                    
//                    if (holder.cb.isChecked() == true) {  
//                        listStr.add(name[position]);  
//                    } else {  
//                        listStr.remove(name[position]);  
//                    }  
                    CAMICUMainTestActivity father = (CAMICUMainTestActivity)getActivity();
    				father.showFragment(CAMICUMainTestActivity.Fragments.TAG_CAM_ICU_RASS_RESULT_FRAGMENT, mBundle, true);
                   
                }  
  
            });  
        }  
    }
    
    
    private String shortName(String str){
		if(str.contains("-")){
			return str.substring(0, str.indexOf("-"));
		}else{
			return str;
		}
		
	}

}
