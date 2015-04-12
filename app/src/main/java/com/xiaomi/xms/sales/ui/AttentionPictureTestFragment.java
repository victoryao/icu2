package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.AttentionImageAdapter;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.GalleryExt;

public class AttentionPictureTestFragment extends BaseFragment {
	
	public int i_position = 0;
	public int[] imgs ;
	private DisplayMetrics dm;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.picture_test_fragment, container,
				false);
		
		// 获得Gallery对象	
		final GalleryExt  g = (GalleryExt) view.findViewById(R.id.ga);
		final Button  btn = (Button) view.findViewById(R.id.submit);
		final ImageView  checkeddot = (ImageView) view.findViewById(R.id.checkeddot);
		btn.setVisibility(View.GONE);
		checkeddot.setVisibility(View.GONE);
		i_position = 0;	 
		imgs = Constants.mFirstPictures;	 
		
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramView) {
				btn.setVisibility(View.INVISIBLE);
				checkeddot.setVisibility(View.GONE);
				if(btn.getText().equals(getActivity().getString(R.string.picture_test_end))
						){
					int num = 0;
					for(int i : Constants.correctAnswer){
						if (!Constants.picSet.contains(i)) {
							num+=1;
						}
					}
					if(num > 3){
						Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT_BOOLEAN, true);
					}else{
						Utils.Preference.setBooleanPref(getActivity(), Constants.IcuGradeResult.ATTENTION_RESULT_BOOLEAN, false);
					}
					Toast.makeText(getActivity(),"提交成功，答错"+num+"个题请滑动屏幕进行思维测试", Toast.LENGTH_SHORT).show();  
					Constants.picSet.clear();
					getActivity().finish();
					return;
				}
				AttentionImageAdapter ia=new AttentionImageAdapter(getActivity(),Constants.mSecondPictures);		
				g.setAdapter(ia);
			 	g.setSelection(i_position); 
			 	g.setBeginBtn(btn);
			 	g.setCheckedDot(checkeddot);
			 	g.setOnItemClickListener(new OnItemClickListener()  
			    {  
			      public void onItemClick(AdapterView parent, View v, int position, long id)  
			      {  
			    	  
			    	  if(Constants.picSet.contains(position)){
				        	 Toast.makeText(getActivity(),"已取消", Toast.LENGTH_SHORT).show();  
						     Constants.picSet.remove(position);
						     checkeddot.setVisibility(View.GONE);
						     return;
				        }
			    	if(Constants.picSet.size() > 4){
				    	Toast.makeText(getActivity(),"选择的图片不能超过5个", Toast.LENGTH_SHORT).show();
				    	return;
				    }
			    	if(!Constants.picSet.contains(position)){
			        	 Toast.makeText(getActivity(),"已选择", Toast.LENGTH_SHORT).show();  
					     Constants.picSet.add(position);
					     checkeddot.setVisibility(View.VISIBLE);
			        }
			        
			      }  
			    }); 
			 	
			 	//加载动画
			 	Animation an= AnimationUtils.loadAnimation(getActivity(),R.anim.scale );
		        g.setAnimation(an); 
			}
		});
		// 添加ImageAdapter给Gallery对象
		AttentionImageAdapter ia=new AttentionImageAdapter(getActivity(),imgs);		
		g.setAdapter(ia);
	 	g.setSelection(i_position); 
	 	g.setBeginBtn(btn);
	 	//加载动画
	 	Animation an= AnimationUtils.loadAnimation(getActivity(),R.anim.scale );
        g.setAnimation(an); 
		return view;
	}

}
