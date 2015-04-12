
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.Order;
import com.xiaomi.xms.sales.util.Utils;

public class OrderLayout extends TableLayout {
    private TextView mOrderId;
    private TextView mFee;
    private TextView mRecipient;
    private TextView mRecipientPhoneNumber;
    private TextView mDetailAddress;
    private TextView mDeleveryTimeTitle;
    private TextView mDeliveryTime;
    private TextView mInvoiceInfo;
    private TextView mOrderPaymentTitle;
    private TextView mPickupPersonNameText;
    private TextView mPickupAddressText;
    private TextView mPickupTelText;

    public OrderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPickupPersonNameText = (TextView) findViewById(R.id.pickup_person_name);
        mPickupAddressText = (TextView) findViewById(R.id.pickup_address_text);
        mPickupTelText = (TextView) findViewById(R.id.pickup_tel_text);
        mOrderId = (TextView) findViewById(R.id.order_id);
        mFee = (TextView) findViewById(R.id.order_fee);
        mRecipient = (TextView) findViewById(R.id.order_recipient);
        mRecipientPhoneNumber = (TextView) findViewById(R.id.order_recipient_phone);
        mDetailAddress = (TextView) findViewById(R.id.order_detail_address);
        mDeleveryTimeTitle = (TextView) findViewById(R.id.order_delivery_time_title);
        mDeliveryTime = (TextView) findViewById(R.id.order_delivery_time);
        mInvoiceInfo = (TextView) findViewById(R.id.order_invoice_info);
        mOrderPaymentTitle = (TextView) findViewById(R.id.order_payment_tile);
    }

    public void fillOrderTable(Order orderInfo) {
        if (orderInfo == null) {
            return;
        }
        mOrderId.setText(orderInfo.getOrderId());
        mFee.setText(Utils.Money.valueOf(orderInfo.getFee()));
        mInvoiceInfo.setText(orderInfo.getInvoiceInfo());
        mOrderPaymentTitle.setText(R.string.order_table_unpayment);
        if (orderInfo.getPickupInfo() == null) {
            mRecipient.setText(orderInfo.getConsignee());
            mRecipientPhoneNumber.setText(orderInfo.getConsigneePhone());
            mDetailAddress.setText(orderInfo.getConsigneeAddress());
            mDeliveryTime.setText(orderInfo.getDeliveryTime());
        } else {
            mPickupPersonNameText.setText(orderInfo.getConsignee() + " "
                    + orderInfo.getConsigneePhone());
            mPickupAddressText.setText(orderInfo.getPickupInfo().mPickupAddress);
            mPickupTelText.setText(orderInfo.getPickupInfo().mPickupTel);
        }

        if (orderInfo.isMihomeBuy()) {
            mDeliveryTime.setVisibility(View.GONE);
            mDeleveryTimeTitle.setVisibility(View.GONE);
        }
    }
}
