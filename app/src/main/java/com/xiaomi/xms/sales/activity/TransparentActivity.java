package com.xiaomi.xms.sales.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.xmsf.account.ui.LoginInputFragment;

/**
 * Created by yaoqiang on 15/4/12.
 */
public class TransparentActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_activity);
        Intent intent=getIntent();
        String str=intent.getStringExtra("extra");
        TextView tv = (TextView)this.findViewById(R.id.transparent_textview);
        tv.setText(str);
    }

}
