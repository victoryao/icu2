package com.xiaomi.xms.sales.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ui.SavePrinterFragment;

public class SavePrinterActivity extends BaseActivity{
    private Bundle mBundle;

    private LinearLayout cart;
    public final static String TAG_SAVE_FRAGMENT = "save_fragment";
    private SavePrinterFragment savePrinterFragment;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setCustomContentView(R.layout.save_printer_activity);
		cart = (LinearLayout)findViewById(R.id.title_right_bar);
	    cart.setVisibility(View.GONE);
	        
		mBundle = getIntent().getExtras();
		showFragment(TAG_SAVE_FRAGMENT, mBundle, false);
        setTitle(R.string.printer_select);
	}

	
   @Override
   protected Fragment newFragmentByTag(String tag) {
	   Fragment fragment = null;
	   if (TextUtils.equals(tag, TAG_SAVE_FRAGMENT)) {
		   savePrinterFragment = new SavePrinterFragment();
		   fragment = savePrinterFragment;
	   }
	  
       return fragment;
   }
}
