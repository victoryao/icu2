
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.BaseSpinnerAdapter;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ShoppingLoader;
import com.xiaomi.xms.sales.loader.ShoppingLoader.Result;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.SelectableProduct;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.ui.ShoppingFragment.OnCheckStatusListener;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.SelfBindView.SelfBindViewInteface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class EditCartItemFragment extends BaseFragment implements
        LoaderCallbacks<ShoppingLoader.Result> {
    private ArrayList<String> mSpinnerlist = new ArrayList<String>();
    private String mItemId;
    private EmptyLoadingView mLoadingView;
    private TextView mTitle;
    private BaseSpinnerAdapter mTitleSpinnerAdapter;
    private Spinner mTitleSpinner;
    private TextView mPrice;
    private TextView mPriceNewline;
    private SelfBindView mPhoto;
    private BaseSpinnerAdapter mAdapter;
    private Spinner mSpinner;
    private final static String TAG = "EditCartItemFragment";
    private Button mDelete;
    private int mOldCount = 0;
    private LinearLayout mContainerLayout;
    // Adapt phone related
    private View mAdaptPhoneContainer;
    private LinearLayout mAdaptPhoneTypes;
    private View mOpContainer;
    private OnCheckStatusListener mCheckStatusListener;
    private String mMihomeBuyId;
    private ArrayList<SelectableProduct> mSelectableProducts;
    private boolean mIsTitleSpinnerInited;
    private LinearLayout mPhotoContainer;
    private String mItemIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        setItemId(bundle.getString(Tags.EditConsumption.ITEM_ID));
        mItemIds = bundle.getString(Tags.EditConsumption.ITEM_IDS);
        mMihomeBuyId = bundle.getString(Constants.Intent.EXTRA_MIHOME_BUY);

        View view = inflater.inflate(R.layout.edit_cartitem_fragment, container, false);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mContainerLayout = (LinearLayout) view.findViewById(R.id.edit_cart_item_container);
        mTitle = (TextView) view.findViewById(R.id.title);
        mTitleSpinner = (Spinner) view.findViewById(R.id.title_spinner);
        mTitleSpinnerAdapter = new BaseSpinnerAdapter(getActivity());
        mTitleSpinner.setAdapter(mTitleSpinnerAdapter);
        mTitleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mIsTitleSpinnerInited) {
                    mIsTitleSpinnerInited = true;
                    return;
                }
                if (mSelectableProducts != null
                        && position < mSelectableProducts.size()) {
                    if (mCheckStatusListener != null) {
                        NextStepInfo nextStep = new NextStepInfo();
                        nextStep.selectableProduct = mSelectableProducts.get(position);
                        mCheckStatusListener.onDelShoppingCartItem(mItemId, nextStep, mItemIds);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mPrice = (TextView) view.findViewById(R.id.price);
        mPriceNewline = (TextView) view.findViewById(R.id.price_newline);
        mPhotoContainer = (LinearLayout) view.findViewById(R.id.container_photo);
        // mPhoto = (ImageView) view.findViewById(R.id.photo);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mAdaptPhoneContainer = view.findViewById(R.id.adapt_phone_container);
        mAdaptPhoneTypes = (LinearLayout) view.findViewById(R.id.adapt_type_view);
        mAdapter = new BaseSpinnerAdapter(getActivity());
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String number = mSpinnerlist.get(position);
                int n = Integer.parseInt(number);
                if (mOldCount == 0 || mOldCount == n) {
                    return;
                }
                Intent intent = new Intent(getActivity(), ShopIntentService.class);
                intent.setAction(Constants.Intent.ACTION_EDIT_CONSUMPTION);
                JSONObject json = new JSONObject();
                try {
                    json.put(Tags.EditConsumption.ITEM_ID, mItemId);
                    json.put(Tags.EditConsumption.ITEM_IDS, mItemIds);
                    json.put(Tags.EditConsumption.CONSUMPTION, number);
                    intent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON,
                            json.toString());
                    intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeBuyId);
                    getActivity().startService(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        mDelete = (Button) view.findViewById(R.id.button_delete);
        mDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckStatusListener != null) {
                    mCheckStatusListener.onDelShoppingCartItem(mItemId,
                            OnCheckStatusListener.NEXT_ACTION_BACK, mItemIds);
                }
            }
        });
        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mOpContainer = view.findViewById(R.id.op_container);
        mOpContainer.setVisibility(View.VISIBLE);
        initImageContainer();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    public void setItemId(String itemId) {
        LogUtil.d(TAG, "setItemId: " + itemId);
        mItemId = itemId;
    }

    @Override
    public Loader onCreateLoader(int arg0, Bundle arg1) {
        mLoader = new ShoppingLoader(getActivity(), mMihomeBuyId);
        mLoader.setProgressNotifiable(mLoadingView);
        return mLoader;
    }

    private void initImageContainer() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final LinearLayout parentView = (LinearLayout) layoutInflater.inflate(R.layout.selfbind_container,
                null);
        mPhoto = (SelfBindView) parentView.findViewById(R.id.selfbind_image);
        mPhoto.SelfBindViewCallBack = new SelfBindViewInteface() {
            @Override
            public void bindView(ImageView view, Bitmap bitmap, Image image) {
                parentView.setLayoutParams(new LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, bitmap.getHeight()
                                * Device.DISPLAY_WIDTH / bitmap.getWidth()));
                view.setImageBitmap(image.proccessImage(bitmap));
            }
        };
        mPhotoContainer.addView(parentView);
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result result) {
        ArrayList<Item> items = result.mInfo.getItems();
        CartListNode node = null;
        Item item = null;
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            if (item.getType() != Item.TYPE_CARTLIST) {
                continue;
            }
            node = (CartListNode) items.get(i).getNode();
            if (!TextUtils.equals(node.getItemId(), mItemId) ||
                    !TextUtils.equals(node.getItemIds(), mItemIds)) {
                continue;
            }

            mSelectableProducts = node.getSelectableProducts();
            if (mSelectableProducts != null && !mSelectableProducts.isEmpty()) {
                mTitle.setVisibility(View.GONE);
                mTitleSpinner.setVisibility(View.VISIBLE);
                mPrice.setVisibility(View.GONE);
                mPriceNewline.setVisibility(View.VISIBLE);
                mPriceNewline
                        .setText(getString(R.string.home_product_price_format, node.getPrice()));
                int selection = 0;
                ArrayList<String> names = new ArrayList<String>();
                int pIdx = 0;
                for (SelectableProduct product : mSelectableProducts) {
                    names.add(product.name);
                    if (product.name.equals(node.getTitle())) {
                        selection = pIdx;
                    }
                    pIdx++;
                }
                mTitleSpinnerAdapter.updateData(names);
                mTitleSpinner.setSelection(selection, false);
            } else {
                mTitle.setVisibility(View.VISIBLE);
                mTitleSpinner.setVisibility(View.GONE);
                mTitle.setText(node.getTitle());
                mPrice.setVisibility(View.VISIBLE);
                mPriceNewline.setVisibility(View.GONE);
                mPrice.setText(getString(R.string.home_product_price_format, node.getPrice()));
            }
            // mItemIds = node.getItemIds();
            ImageLoader.getInstance()
                    .loadImage(mPhoto, node.getPhoto(), R.drawable.default_pic_large);

            setAdaptPhoneView(node);

            mSpinnerlist.clear();
            int buyLimit = node.getBuyLimit();

            for (int m = 1; m <= buyLimit; m++) {
                mSpinnerlist.add("" + m);
            }

            mAdapter.updateData(mSpinnerlist);

            for (int j = 0; j < mSpinnerlist.size(); j++) {
                if (node.getCount() == Integer.parseInt(mSpinnerlist.get(j))) {
                    mOldCount = node.getCount();
                    mSpinner.setSelection(j, true);
                }
            }
            if (!node.getCanChangeNum()) {
                mSpinner.setVisibility(View.GONE);
            } else {
                mSpinner.setVisibility(View.VISIBLE);
            }

            if (!node.getCanDelete()) {
                mDelete.setVisibility(View.GONE);
            } else {
                mDelete.setVisibility(View.VISIBLE);
            }
            break;
        }

        if (mContainerLayout != null) {
            mContainerLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.edit_cart_item_activity_title);
    }

    public ShoppingActivity getParent() {
        return (ShoppingActivity) getActivity();
    }

    public void onSubmitCallback(String action, Intent callbackIntent) {
        if (TextUtils.equals(action, Constants.Intent.ACTION_EDIT_CONSUMPTION)) {
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    private void setAdaptPhoneView(CartListNode node) {
        mAdaptPhoneContainer.setVisibility(View.GONE);
        mAdaptPhoneTypes.removeAllViews();
        if (node.getAdaptPhone() != null) {
            Set<String> set = node.getAdaptPhone().keySet();
            String[] adaptArray = set.toArray(new String[set.size()]);
            if (adaptArray != null) {
                mAdaptPhoneContainer.setVisibility(View.VISIBLE);
                for (int i = 0; i < adaptArray.length; i++) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    LinearLayout view = (LinearLayout) layoutInflater.inflate(
                            R.layout.adapt_phone_item, null);
                    View phoneType = view.findViewById(R.id.phone_type);
                    view.setTag(R.id.phone_type, phoneType);
                    TextView type = (TextView) view.getTag(R.id.phone_type);
                    type.setText(node.getAdaptPhone().get(adaptArray[i]));
                    if (TextUtils.equals(Tags.Phone.M11S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m11s_icon);
                    } else if (TextUtils.equals(Tags.Phone.M22S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m22s_icon);
                    } else if (TextUtils.equals(Tags.Phone.MI_BOX, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m11s_icon);
                    } else if (TextUtils.equals(Tags.Phone.M2A_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m2a_icon);
                    } else if (TextUtils.equals(Tags.Phone.MRED_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mred_icon);
                    } else if (TextUtils.equals(Tags.Phone.M3_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m3_icon);
                    } else if (TextUtils.equals(Tags.Phone.MI_TV, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mtv_icon);
                    } else if (TextUtils.equals(Tags.Phone.ALL_PHONETYPE, adaptArray[i])) {
                        type.setTextColor(getResources().getColor(
                                R.color.highlight_text_color_inverse));
                        type.setTextSize(14);
                    } else {
                        type.setBackgroundResource(R.drawable.m11s_icon);
                    }
                    mAdaptPhoneTypes.addView(view);
                }
            }
        }
    }

    public void setOnCheckStatusListener(OnCheckStatusListener l) {
        mCheckStatusListener = l;
    }

    public static class NextStepInfo {
        public SelectableProduct selectableProduct;
    }
}
