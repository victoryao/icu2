
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.CommentItemInfo;
import com.xiaomi.xms.sales.ui.CommentItem;

public class CommentListAdapter extends BaseDataAdapter<CommentItemInfo> {
    public CommentListAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, CommentItemInfo data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
    }

    @Override
    public void bindView(View view, int position, CommentItemInfo data) {
        if (view instanceof CommentItem) {
            ((CommentItem) view).bind(data);
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
    }
}
