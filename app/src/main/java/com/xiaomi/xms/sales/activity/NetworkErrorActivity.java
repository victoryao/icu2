package com.xiaomi.xms.sales.activity;

import com.xiaomi.xms.sales.R;

import android.os.Bundle;

public class NetworkErrorActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setTitle(R.string.title_network);
		setCustomContentView(R.layout.network_error_activty);
	}
      
}
