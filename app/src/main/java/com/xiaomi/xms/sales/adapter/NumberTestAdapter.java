package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class NumberTestAdapter extends  BaseAdapter {
		    // 定义Context
		    private Context     mContext;
		    // 定义整型数组 即图片源
		    private Integer[]   mImageIds   =
		    {
		    		R.string.number_test_n1,
		    		R.string.number_test_n2,
		    		R.string.number_test_n3,
		    		R.string.number_test_n4,
		    		R.string.number_test_n5,
		    		R.string.number_test_n6,
		    		R.string.number_test_n7,
		    		R.string.number_test_n8,
		    		R.string.number_test_n9,
		    		R.string.number_test_n10
		    };
		 
		    public NumberTestAdapter(Context c)
		    {
		        mContext = c;
		    }
		 
		    // 获取图片的个数
		    public int getCount()
		    {
		        return mImageIds.length;
		    }
		 
		    // 获取图片在库中的位置
		    public Object getItem(int position)
		    {
		        return position;
		    }
		 
		 
		    // 获取图片ID
		    public long getItemId(int position)
		    {
		        return position;
		    }
		 
		 
		    public View getView(int position, View convertView, ViewGroup parent)
		    {
		        TextView textView;
		        if (convertView == null)
		        {
		            // 给ImageView设置资源
		            textView = new TextView(mContext);
		            // 设置布局 图片120×120显示
		            textView.setLayoutParams(new GridView.LayoutParams(60, 90));
		            textView.setTextSize(30);
		            
		        }
		        else
		        {
		            textView = (TextView) convertView;
		        }
		        if(mContext.getResources().getString(mImageIds[position]).equals("8")){
		        	textView.setTextColor(Color.RED);
		        }
		        textView.setText(mImageIds[position]);
		        return textView;
		    }
		 
		}
