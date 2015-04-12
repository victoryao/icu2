package com.xiaomi.xms.sales.adapter;

import android.R.color;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiaomi.xms.sales.widget.GalleryExt;

/**
 * 
 * @author 空山不空
 *  图片适配器，用来加载图片
 */
public class AttentionImageAdapter extends BaseAdapter {
//图片适配器
	// 定义Context 
	private int ownposition;
	private int[] imgres;

	public int[] getImgres() {
		return imgres;
	}

	public void setImgres(int[] imgres) {
		this.imgres = imgres;
	}

	public int getOwnposition() {
		return ownposition;
	}

	public void setOwnposition(int ownposition) {
		this.ownposition = ownposition;
	}

	private Context mContext; 

	// 定义整型数组 即图片源

	// 声明 ImageAdapter
	public AttentionImageAdapter(Context c, int[] imgs) {
		mContext = c;
		imgres = imgs;
	}

	// 获取图片的个数
	public int getCount() {
		return imgres.length;
	}

	// 获取图片在库中的位置
	public Object getItem(int position) { 
		ownposition=position;
		return position;
	}

	// 获取图片ID
	public long getItemId(int position) {
		ownposition=position; 
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		 
		ownposition=position;
		ImageView imageview = new ImageView(mContext);
		
		imageview.setBackgroundColor(color.white);
		imageview.setScaleType(ImageView.ScaleType.FIT_XY);
		imageview.setLayoutParams(new GalleryExt.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageview.setImageResource(imgres[position]);
		// imageview.setAdjustViewBounds(true);
		// imageview.setLayoutParams(new GridView.LayoutParams(320, 480));
		// imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		return imageview;
	}
}
