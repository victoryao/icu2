
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.AddressActivity;
import com.xiaomi.xms.sales.adapter.AddressInfoAdapter;
import com.xiaomi.xms.sales.loader.AddressInfoLoader;
import com.xiaomi.xms.sales.loader.AddressInfoLoader.Result;
import com.xiaomi.xms.sales.model.AddressInfo;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class AddressListFragment extends BaseFragment implements
        LoaderCallbacks<AddressInfoLoader.Result> {
    private final static String TAG = "AddressListFragment";
    private BaseListView mListView;
    private View mFooterView;
    private EmptyLoadingView mLoadingView;
    private AddressInfoAdapter mAdapter;
    private RelativeLayout mAddButtonBottom;
    private String mAction;
    private String mAddressId;
    private ProgressDialog mProgressDialog;
    private final static int AddressList_LOADER = 0;

    private final static int MENU_EDIT = 1;
    private final static int MENU_DEL = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAddressId = args.getString(Constants.Intent.EXTRA_ADDRESS_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.address_list_fragment, container, false);

        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mFooterView = inflater.inflate(R.layout.address_list_footer, null, false);
        mListView.addFooterView(mFooterView);

        mAddButtonBottom = (RelativeLayout) view.findViewById(R.id.add_address_bottom);

        mAddButtonBottom.setOnClickListener(mAddButtonListener);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.tips_empty_address);

        if (TextUtils.equals(mAction, Constants.Intent.ACTION_EDIT_ADDRESS)) {
            mListView.setOnCreateContextMenuListener(this);
            mAdapter = new AddressInfoAdapter(getActivity());
        } else {
            mAdapter = new AddressInfoAdapter(getActivity(), mAddressId);
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemClickListener);
        if (mLoader != null) {
            AddressActivity father = (AddressActivity) getActivity();
            if (father.isAddressListReload()) {
                mLoader.reload();
                father.setAddressListReload(false);
            }
        }
        return view;
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
            if (TextUtils.equals(mAction, Constants.Intent.ACTION_USE_ADDRESS)) {
                String addressId = ((AddressInfo) item.getTag()).getAddressId();
                mAdapter.setCheckedAddressId(addressId);
                Intent data = new Intent();
                data.putExtra(Constants.Intent.EXTRA_ADDRESS_ID, addressId);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            } else if (TextUtils.equals(mAction, Constants.Intent.ACTION_EDIT_ADDRESS)) {
                startEditAddress(item);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(AddressList_LOADER, null, this);

    }

    private OnClickListener mAddButtonListener = new OnClickListener() {
        @Override
        public void onClick(View btn) {
            startEditAddress(btn);
        }
    };

    public void setAction(String action) {
        mAction = action;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == AddressList_LOADER) {
            mLoader = new AddressInfoLoader(getActivity());
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mAdapter.updateData(data.mAddressInfos);
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_EDIT:
                startEditAddress(info.targetView);
                return true;
            case MENU_DEL:
                BaseAlertDialog dialog = new BaseAlertDialog(getActivity());
                dialog.setTitle(R.string.address_del);
                dialog.setMessage(getResources().getString(R.string.address_list_del_ask));
                dialog.setPositiveButton(R.string.dialog_ask_ok, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delAddressItem(info.targetView);
                    }
                });
                dialog.setNegativeButton(R.string.dialog_ask_cancel, null);
                dialog.show();
                return true;
            default:
        }
        return super.onContextItemSelected(item);
    }

    private void startEditAddress(View item) {
        AddressInfo address = (AddressInfo) item.getTag();
        AddressActivity activity = (AddressActivity) getActivity();
        if (address != null) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_ADDRESS_ID, address.getAddressId());
            bundle.putString(Constants.Intent.EXTRA_ADDRESS_CONSIGNEE, address.getConsignee());
            bundle.putInt(Constants.Intent.EXTRA_ADDRESS_PROVINCE, address.getProvinceId());
            bundle.putInt(Constants.Intent.EXTRA_ADDRESS_CITY, address.getCityId());
            bundle.putInt(Constants.Intent.EXTRA_ADDRESS_DISTRICT, address.getDistrictId());
            bundle.putString(Constants.Intent.EXTRA_ADDRESS_LOCATION, address.getAddress());
            bundle.putString(Constants.Intent.EXTRA_ADDRESS_ZIPCODE, address.getZipCode());
            bundle.putString(Constants.Intent.EXTRA_ADDRESS_TEL, address.getTel());
            activity.showFragment(AddressActivity.TAG_ADD_FRAGMENT, bundle, true);
        } else {
            activity.showFragment(AddressActivity.TAG_ADD_FRAGMENT, null, true);
        }

    }

    public void delAddressItemComplete(int result, String error) {
        mProgressDialog.dismiss();
        switch (result) {
            case Request.STATUS_OK:
                ToastUtil.show(getActivity(), R.string.address_ok);
                mLoader.reload();
                break;
            case Request.STATUS_NETWORK_UNAVAILABLE:
                ToastUtil.show(getActivity(), R.string.network_unavaliable);
                break;
            default:
                if (TextUtils.isEmpty(error)) {
                    ToastUtil.show(getActivity(), R.string.address_err);
                } else {
                    ToastUtil.show(getActivity(), error);
                }
        }
    }

    private void delAddressItem(View item) {
        mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.address_deling),
                getString(R.string.address_deling));
        mProgressDialog.setCancelable(true);

        String addressId = ((AddressInfo) item.getTag()).getAddressId();
        Intent serviceIntent = new Intent(getActivity(), ShopIntentService.class);
        serviceIntent.setAction(Constants.Intent.ACTION_DEL_ADDRESS);
        serviceIntent.putExtra(Constants.Intent.EXTRA_ADDRESS_ID, addressId);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        LogUtil.d(TAG, "onCreateContextMenu");
        View selectedItem = ((AdapterContextMenuInfo) menuInfo).targetView;
        String title = (String) ((TextView) (selectedItem.findViewById(R.id.address_consignee)))
                .getText();
        menu.setHeaderTitle(title);
        menu.add(0, MENU_EDIT, MENU_EDIT, R.string.address_edit);
        menu.add(0, MENU_DEL, MENU_DEL, R.string.address_del);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoader != null) {
            mLoader.forceLoad();
        }
    }
}
