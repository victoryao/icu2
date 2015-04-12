
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.ShoppingAdapter;
import com.xiaomi.xms.sales.loader.ShoppingLoader;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode.IncastProduct;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.SupplyNode;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class ShoppingFragment extends BaseFragment implements
        LoaderCallbacks<ShoppingLoader.Result>, OnItemClickListener {
    private static final String TAG = "ShoppingFragment";

    private final static int CARTLIST_LOADER = 0;
    private EmptyLoadingView mLoadingView;
    private BaseListView mListView;
    private ShoppingAdapter mAdapter;
    private View mEmpty;
    private View mContainer;
    public TextView mPrice;
    public Button mCheckout;
    private View mHeaderContainer;
    private Button mGoto;
    private OnCheckStatusListener mCheckStatusListener;
    private String mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMihomeBuyId = bundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);
            if (TextUtils.isEmpty(mMihomeBuyId)) {
                mMihomeBuyId = HostManager.Parameters.Values.MIHOME_BUY_NULL;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shopping_fragment, container, false);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        mAdapter = new ShoppingAdapter(getActivity());
        View footerView = inflater.inflate(R.layout.empty_list_item, null, false);
        mListView.addFooterView(footerView, null, false);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mContainer = view.findViewById(R.id.container);

        mPrice = (TextView) view.findViewById(R.id.total);
        mCheckout = (Button) view.findViewById(R.id.next);
        mCheckout.setText(getString(R.string.shopping_button_checkout));
        mCheckout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ShoppingActivity activity = (ShoppingActivity) getActivity();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeBuyId);
                activity.showFragment(ShoppingActivity.Fragments.TAG_ORDER_SUBMIT_FRAGMENT, bundle,true);
            }
        });
        mHeaderContainer = view.findViewById(R.id.header_container);
        mEmpty = (View) view.findViewById(R.id.empty);
        showEmptyView(false);
        mGoto = (Button) view.findViewById(R.id.goto_button);
        mGoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.launchMain(getActivity(), MainActivity.FRAGMENT_TAG_CATEGORY);
            }
        });
        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!LoginManager.getInstance().hasLogin()) {
            getParent().gotoAccount();
        }
        getLoaderManager().initLoader(CARTLIST_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.product_shopping_cart);
    }

    @SuppressWarnings("unchecked")
    public Loader<ShoppingLoader.Result> onCreateLoader(int id, Bundle bundle) {
        if (id == CARTLIST_LOADER) {
            mHeaderContainer.setVisibility(View.GONE);
            mLoader = new ShoppingLoader(getActivity(), mMihomeBuyId);
            mLoader.setProgressNotifiable(mLoadingView);
            mLoader.setNeedDatabase(false);
            return (Loader<ShoppingLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ShoppingLoader.Result> loader, ShoppingLoader.Result data) {
        if (ShoppingActivity.Signal.CART_RELOAD == true) {
            ShoppingActivity.Signal.CART_RELOAD = false;
            getLoaderManager().restartLoader(CARTLIST_LOADER, null, this);
            return;
        }
        if (data.mInfo == null) {
            return;
        }
        LogUtil.d(TAG, "onLoadFinished: data.result is " + data.mInfo.getResult());
        if (!TextUtils.equals(String.valueOf(data.mInfo.getCode()), Tags.RESULT_CODE)) {
            Toast.makeText(getActivity(), data.mInfo.getDescription(), Toast.LENGTH_SHORT).show();
            return;
        }

        LogUtil.d(TAG, "onLoadFinished:" + data.mInfo);

        if (data.mInfo.hasEmpty()) {
            showEmptyView(true);
        } else {
            mAdapter.updateData(data.mInfo.getItems());
            mPrice.setText(String.format(getString(R.string.currency_unit_template),
                    data.mInfo.getTotal()));
            mHeaderContainer.setVisibility(View.VISIBLE);
        }

        ((BaseActivity) getActivity()).updateShoppingCount();
    }

    @Override
    public void onLoaderReset(Loader<ShoppingLoader.Result> loader) {

    }

    @Override
    protected void onNetworkConnected(int type) {
        if (mLoader != null) {
            mLoader.reload();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        int type = mAdapter.getItemViewType(position);
        if (type == Item.TYPE_CARTLIST) {
            CartListNode node = (CartListNode) view.getTag();
            String itemId = node.getItemId();
            String itemIds = node.getItemIds();
            ShoppingActivity activity = (ShoppingActivity) getActivity();
            Bundle bundle = new Bundle();
            bundle.putString(Tags.EditConsumption.ITEM_ID, itemId);
            bundle.putString(Tags.EditConsumption.ITEM_IDS, itemIds);
            bundle.putString(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeBuyId);
            activity.showFragment(ShoppingActivity.Fragments.TAG_EDIT_CARTITEM_FRAGMENT, bundle,
                    true);
        } else if (type == Item.TYPE_SUPPLY) {
            Item item = mAdapter.getData().get(position);
            SupplyNode supplyNode = (SupplyNode) item.getNode();
            onSupplyClick(supplyNode);
        } else if (type == Item.TYPE_INCAST) {
            ShoppingActivity avtivity = (ShoppingActivity) getActivity();
            IncastNode node = (IncastNode) mAdapter.getData().get(position).getNode();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Intent.EXTRA_INCAST_PRODUCTS,
                    IncastProduct.serialize(node.getPostFreeProducts()));
            avtivity.showFragment(ShoppingActivity.Fragments.TAG_INCAST_PRODUCTS_FRAGMENT, bundle,
                    true);
        }
    }

    public void showEmptyView(boolean isShow) {
        if (isShow) {
            mEmpty.setVisibility(View.VISIBLE);
            mContainer.setVisibility(View.GONE);
        }
        else {
            mEmpty.setVisibility(View.GONE);
            mContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onSupplyClick(SupplyNode node) {
        LogUtil.d(TAG, "onSupplyClick");
        if (mCheckStatusListener != null) {
            if (!node.getCheckedStatus()) {
                mCheckStatusListener.onAddShoppingCart(node.getActId(), node.getProductId(),
                        ShoppingCartListInfo.PROMOTION_TYPE_SUPPLY, null, "");
            } else {
                mCheckStatusListener.onDelShoppingCartItem(node.getItemId(), null, "");
            }
        }
    }

    public void onSupplyCallback(Intent callbackIntent) {
        String action = callbackIntent.getAction();
        if (TextUtils.equals(action, Constants.Intent.ACTION_ADD_SHOPPING_CART)) {
            String result = callbackIntent.getStringExtra(
                    Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
            String msg = callbackIntent
                    .getStringExtra(Constants.Intent.EXTRA_ADD_SHOPPING_CART_RESULT_MSG);
            if (!TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_SUCCESS)) {
                if (TextUtils.equals(result, Constants.AddShoppingCartStatus.ADD_FAIL)) {
                    ToastUtil.show(getActivity(), R.string.shopping_add_supply_exception);
                } else {
                    ToastUtil.show(getActivity(), msg);
                }
                return;
            }
        } else if (TextUtils.equals(action, Constants.Intent.ACTION_DELETE_CARTITEM)) {
        }
        ShoppingActivity.Signal.CART_RELOAD = false;
        getLoaderManager().restartLoader(CARTLIST_LOADER, null, this);
    }

    public ShoppingActivity getParent() {
        return (ShoppingActivity) getActivity();
    }

    public interface OnCheckStatusListener {
        public static final String NEXT_ACTION_BACK = "back";

        public void onDelShoppingCartItem(String item, Object nextStep, String itemIds);

        public void onAddShoppingCart(String actId, String productId, String promotionType,
                Object nextStep, String itemIds);
    }

    public void setOnCheckStatusListener(OnCheckStatusListener l) {
        mCheckStatusListener = l;
    }
}
