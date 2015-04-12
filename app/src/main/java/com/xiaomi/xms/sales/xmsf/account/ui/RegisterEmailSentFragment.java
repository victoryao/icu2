package com.xiaomi.xms.sales.xmsf.account.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;

public class RegisterEmailSentFragment extends StepsFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.micloud_email_sent, container, false);

        Bundle args = getArguments();
        TextView emailText = (TextView) v.findViewById(R.id.email);
        emailText.setText(args != null ? args.getString("email") : null);

        getActivity().setTitle(R.string.title_activate);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySoftInputIfNeed(getView(), false);
    }

    @Override
    protected void onButtonFinishClicked() {
        getActivity().finish();
    }
}
