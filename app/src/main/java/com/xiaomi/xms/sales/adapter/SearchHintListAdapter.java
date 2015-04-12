
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class SearchHintListAdapter extends BaseDataAdapter<SpannableString> {
    public SearchHintListAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, SpannableString data, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_hint_list_item, null);
    }

    @Override
    public void bindView(View view, int position, SpannableString data) {
        TextView word = (TextView) view.findViewById(R.id.word);
        word.setText(data);
    }

    protected void bindBackground(View view, int position) {
        view.setBackgroundResource(R.drawable.hint_list_item_bg);
    }

}
