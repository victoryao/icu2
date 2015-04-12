
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.util.Constants;

public class MiHomeBuyErrorFragment extends BaseFragment {

    private TextView mResultView;
    private Button mBackView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mihome_buy_error_fragment, container, false);
        mResultView = (TextView) view.findViewById(R.id.result);
        mBackView = (Button) view.findViewById(R.id.back);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String result = bundle.getString(Constants.Intent.EXTRA_MIHOME_ERROR_RESULT);
            mResultView.setText(result);
        }
        mBackView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }
}
