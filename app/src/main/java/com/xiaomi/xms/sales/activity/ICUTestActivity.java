package com.xiaomi.xms.sales.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TabHost;

import com.xiaomi.xms.sales.R;

public class ICUTestActivity extends TabActivity {


	private TabHost tabHost;
	/**
	 * 动画图片
	 */
	private ImageView mTabImg;
	/**
	 * 动画图片偏移量
	 */
	private int zero = 0;
	/**
	 * 第一个水平动画平移大小
	 */
	private int one = 0;
	/**
	 * 当前页卡编号
	 */
	private int currIndex = 0;
	private Animation animation;
	private RadioButton guide_home, guide_risk, guide_action, guide_cart;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.icu_test_main);
		initTab();
		init();
	}

	private void initTab() {
		tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("guide_home")
				.setIndicator("guide_home")
				.setContent(new Intent(this, MainActivity
						.class)));
		tabHost.addTab(tabHost.newTabSpec("guide_risk")
				.setIndicator("guide_risk")
				.setContent(new Intent(this, RiskMainActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("guide_action")
				.setIndicator("guide_action")
				.setContent(new Intent(this, IcuActionActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("guide_cart")
				.setIndicator("guide_cart")
				.setContent(new Intent(this, IcuHistoryActivity.class)));
	}

	private void init() {
		mTabImg = (ImageView) findViewById(R.id.img_tab_now);
		Display currDisplay = getWindowManager().getDefaultDisplay();// 获取屏幕当前分辨率
		int displayWidth = currDisplay.getWidth();
		one = displayWidth / 4 ;

		guide_home = (RadioButton) findViewById(R.id.guide_home);
		guide_risk = (RadioButton) findViewById(R.id.guide_risk);
		guide_action = (RadioButton) findViewById(R.id.guide_action);
		guide_cart = (RadioButton) findViewById(R.id.guide_cart);
		
		guide_home.setOnClickListener(new MyOnPageChangeListener());
		guide_risk.setOnClickListener(new MyOnPageChangeListener());
		guide_action.setOnClickListener(new MyOnPageChangeListener());
		guide_cart.setOnClickListener(new MyOnPageChangeListener());
	}

	private class MyOnPageChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			animation = null;
			switch (v.getId()) {
			case R.id.guide_home:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(one * 2, 0, 0, 0);
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(one * 3, 0, 0, 0);
				} 
				currIndex = 0;
				tabHost.setCurrentTabByTag("guide_home");
				break;
			case R.id.guide_risk:
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(one * 2, one, 0, 0);
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(one * 3, one, 0, 0);
				}
				currIndex = 1;
				tabHost.setCurrentTabByTag("guide_risk");
				break;
			case R.id.guide_action:
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one * 2, 0, 0);
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, one * 2, 0, 0);
				} else if (currIndex == 3) {
					animation = new TranslateAnimation(one * 3, one * 2, 0, 0);
				} 
				currIndex = 2;
				tabHost.setCurrentTabByTag("guide_action");
				break;
			case R.id.guide_cart:
				if (currIndex == 0) {
					animation = new TranslateAnimation(zero, one * 3, 0, 0);
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, one * 3, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(one * 2, one * 3, 0, 0);
				} 
				currIndex = 3;
				tabHost.setCurrentTabByTag("guide_cart");
				break;
			}
			
			if (animation != null) {
				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(150);
				mTabImg.startAnimation(animation);
			}
		}

	}

}