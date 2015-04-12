package com.xiaomi.xms.sales.ui;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.util.Utils;

public class ReturnOrderLayout extends TableLayout {
	private TextView mReturnMerchantNameText;
	private TextView mReturnOrderIdText;
	private TextView mReturnOrderDateText;
	private TextView mReturnPosNameText;
	private TextView mReturnAmountText;
	private TextView mReturnOrderPaymentTitle;

	public ReturnOrderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mReturnMerchantNameText = (TextView) findViewById(R.id.return_merchant_name);
		mReturnOrderIdText = (TextView) findViewById(R.id.return_order_id);
		mReturnOrderDateText = (TextView) findViewById(R.id.return_order_date);
		mReturnPosNameText = (TextView) findViewById(R.id.return_pos_name);
		mReturnAmountText = (TextView) findViewById(R.id.return_amount);
		mReturnOrderPaymentTitle = (TextView) findViewById(R.id.return_order_payment_title);
	}

	public void fillOrderTable(Order orderInfo) {
		if (orderInfo == null) {
			return;
		}
		mReturnMerchantNameText.setText(orderInfo.getmMerchantName());
		mReturnOrderIdText.setText(orderInfo.getOrderId());
		mReturnAmountText.setText(Utils.Money.valueOf(orderInfo.getFee())+"元");
		java.text.DateFormat format1 = new java.text.SimpleDateFormat(  
                "yyyy-MM-dd hh:mm:ss");  
		String s = format1.format(new Date(Long.parseLong(orderInfo.getAddTime())));  
		mReturnOrderDateText.setText(s);
		mReturnPosNameText.setText(orderInfo.getmPosName());
		mReturnOrderPaymentTitle.setText(R.string.return_order_confirm_title);
	}
}
