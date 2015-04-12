
package com.xiaomi.xms.sales.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.CommentItemInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentItem extends BaseListItem<CommentItemInfo> {

    private TextView mContent;
    private TextView mTime;
    private TextView mUserName;
    private RatingBar mAverageGrade;
    private View mBottomLine;

    public CommentItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAverageGrade = (RatingBar) findViewById(R.id.comment_rating_bar);
        mContent = (TextView) findViewById(R.id.comment_content);
        mBottomLine = findViewById(R.id.comment_item_bottom_line);
        mUserName = (TextView) findViewById(R.id.comment_user);
        mTime = (TextView) findViewById(R.id.comment_time);
    }

    @Override
    public void bind(CommentItemInfo info) {
        mAverageGrade.setRating(info.getAverageGrade());
        mContent.setText(info.getContent());
        mUserName.setText(info.getUserName());
        mBottomLine.setVisibility(info.getBottomLineVisibility());
        Date date = new Date(info.getAddTime() * 1000);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        mTime.setText(format.format(date));
    }
}
