
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.BaseSpinnerAdapter;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductDetailsInfo;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.SelectableProduct;
import com.xiaomi.xms.sales.ui.ShoppingFragment.OnCheckStatusListener;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.SelfBindView.SelfBindViewInteface;

import java.util.ArrayList;
import java.util.Set;

/**
 * 仅用于加价购项的详情展示。
 */
public class ShoppingProductFragment extends BaseFragment implements
        LoaderCallbacks<ProductDetailsLoader.Result>, OnClickListener {
    private EmptyLoadingView mLoadingView;
    private TextView mTitle;
    private Spinner mTitleSpinner;
    private BaseSpinnerAdapter mTitleSpinnerAdapter;
    private TextView mPrice;
    private TextView mPriceNewline;
    private SelfBindView mPhoto;
    private Button mAddSupplyBtn, mReplaceSupplyBtn;
    private ProductDetailsInfo mProductDetailInfo;
    private LinearLayout mContainerLayout;
    // Adapt phone related
    private View mAdaptPhoneContainer;
    private LinearLayout mAdaptPhoneTypes;
    private String mActId;
    private String mItemId;
    private String mCurrProductId;
    private String mBoughtProductId;
    private ArrayList<SelectableProduct> mSelectableProducts;
    private int PRODUCT_DETAILS_LOADER = 0;
    private OnCheckStatusListener mCheckStatusListener;
    private LinearLayout mPhotoContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mCurrProductId = bundle.getString(Tags.Product.PRODUCT_ID);
        mBoughtProductId = bundle.getString(Tags.ShoppingSupply.BOUGHT_PRODUCT_ID);
        mActId = bundle.getString(Tags.ShoppingSupply.ACT_ID);
        mItemId = bundle.getString(Tags.ShoppingSupply.ITEM_ID);
        mSelectableProducts = bundle
                .getParcelableArrayList(Tags.ShoppingSupply.SELECTABLE_PRODUCTS);

        View view = inflater.inflate(R.layout.edit_cartitem_fragment, container, false);
        mPrice = (TextView) view.findViewById(R.id.price);
        mPriceNewline = (TextView) view.findViewById(R.id.price_newline);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mContainerLayout = (LinearLayout) view.findViewById(R.id.edit_cart_item_container);
        mTitle = (TextView) view.findViewById(R.id.title);
        mTitleSpinner = (Spinner) view.findViewById(R.id.title_spinner);
        mTitleSpinnerAdapter = new BaseSpinnerAdapter(getActivity());
        mTitleSpinner.setAdapter(mTitleSpinnerAdapter);
        if (mSelectableProducts != null && !mSelectableProducts.isEmpty()) {
            mTitle.setVisibility(View.GONE);
            mTitleSpinner.setVisibility(View.VISIBLE);
            mPrice.setVisibility(View.GONE);
            mPriceNewline.setVisibility(View.VISIBLE);
            int selection = 0;
            ArrayList<String> names = new ArrayList<String>();
            int pIdx = 0;
            for (SelectableProduct product : mSelectableProducts) {
                names.add(product.name);
                if (product.productId.equals(mCurrProductId)) {
                    selection = pIdx;
                }
                pIdx++;
            }
            mTitleSpinnerAdapter.updateData(names);
            mTitleSpinner.setSelection(selection, false);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mTitleSpinner.setVisibility(View.GONE);
            mPrice.setVisibility(View.VISIBLE);
            mPriceNewline.setVisibility(View.GONE);
        }
        mTitleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSelectableProducts != null
                        && position < mSelectableProducts.size()) {
                    mCurrProductId = mSelectableProducts.get(position).productId;
                    setSupplyButtons();
                    ((ProductDetailsLoader) mLoader).setProductId(mCurrProductId);
                    mLoader.forceLoad();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mAddSupplyBtn = (Button) view.findViewById(R.id.button_add_supply);
        mReplaceSupplyBtn = (Button) view.findViewById(R.id.button_replace_supply);
        mAddSupplyBtn.setOnClickListener(this);
        mReplaceSupplyBtn.setOnClickListener(this);
        setSupplyButtons();
        mPhotoContainer = (LinearLayout) view.findViewById(R.id.container_photo);
        mAdaptPhoneContainer = view.findViewById(R.id.adapt_phone_container);
        mAdaptPhoneTypes = (LinearLayout) view.findViewById(R.id.adapt_type_view);

        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        initImageContainer();
        return view;
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

    private void setSupplyButtons() {
        if (mBoughtProductId != null) {
            if (mSelectableProducts != null && !mSelectableProducts.isEmpty()) {
                mAddSupplyBtn.setVisibility(View.GONE);
                mReplaceSupplyBtn.setVisibility(View.VISIBLE);
                mReplaceSupplyBtn.setEnabled(!mBoughtProductId.equals(mCurrProductId));
                mReplaceSupplyBtn.setText(mReplaceSupplyBtn.isEnabled() ? R.string.replace
                        : R.string.already_add_shopping_cart);
            } else {
                mAddSupplyBtn.setVisibility(View.VISIBLE);
                mReplaceSupplyBtn.setVisibility(View.GONE);
                mAddSupplyBtn.setEnabled(false);
                mAddSupplyBtn.setText(R.string.already_add_shopping_cart);
            }
        } else {
            mAddSupplyBtn.setVisibility(View.VISIBLE);
            mReplaceSupplyBtn.setVisibility(View.GONE);
            mAddSupplyBtn.setEnabled(true);
            mAddSupplyBtn.setText(R.string.add_shopping_cart);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mAddSupplyBtn) {
            if (mCheckStatusListener != null) {
                mCheckStatusListener.onAddShoppingCart(mActId, mCurrProductId,
                        ShoppingCartListInfo.PROMOTION_TYPE_SUPPLY,
                        OnCheckStatusListener.NEXT_ACTION_BACK, "");
            }
        } else if (view == mReplaceSupplyBtn) {
            if (mCheckStatusListener != null) {
                NextStepInfo nextStep = new NextStepInfo();
                nextStep.actId = mActId;
                nextStep.productId = mCurrProductId;
                nextStep.promotionType = ShoppingCartListInfo.PROMOTION_TYPE_SUPPLY;
                mCheckStatusListener.onDelShoppingCartItem(mItemId, nextStep, "");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PRODUCT_DETAILS_LOADER, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == PRODUCT_DETAILS_LOADER) {
            mLoader = new ProductDetailsLoader(getActivity());
            ((ProductDetailsLoader) mLoader).setProductId(mCurrProductId);
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ProductDetailsLoader.Result> loader,
            ProductDetailsLoader.Result result) {
        mProductDetailInfo = result.mProductDetailsInfos;
        if (mProductDetailInfo == null) {
            return;
        }
        mTitle.setText(mProductDetailInfo.getProductName());
        mPrice.setText(getString(R.string.home_product_price_format,
                mProductDetailInfo.getProductPrice()));
        mPriceNewline.setText(getString(R.string.home_product_price_format,
                mProductDetailInfo.getProductPrice()));

        ImageLoader.getInstance()
                .loadImage(mPhoto, mProductDetailInfo.getSupplyImage(),
                        R.drawable.default_pic_small);

        setAdaptPhoneView(mProductDetailInfo);

        if (mContainerLayout != null) {
            mContainerLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ProductDetailsLoader.Result> arg0) {

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.product_detail);
    }

    public ShoppingActivity getParent() {
        return (ShoppingActivity) getActivity();
    }

    public void onSubmitCallback(String action, Intent callbackIntent) {
        if (TextUtils.equals(action, Constants.Intent.ACTION_EDIT_CONSUMPTION)) {
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    private void setAdaptPhoneView(ProductDetailsInfo productDetailInfo) {
        mAdaptPhoneContainer.setVisibility(View.GONE);
        mAdaptPhoneTypes.removeAllViews();
        if (productDetailInfo.getAdaptPhone() != null) {
            Set<String> set = productDetailInfo.getAdaptPhone().keySet();
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
                    type.setText(productDetailInfo.getAdaptPhone().get(adaptArray[i]));
                    if (TextUtils.equals(Tags.Phone.M11S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m11s_icon);
                    } else if (TextUtils.equals(Tags.Phone.M22S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m22s_icon);
                    } else if (TextUtils.equals(Tags.Phone.MI_BOX, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mbox_icon);
                    } else if (TextUtils.equals(Tags.Phone.M2A_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m2a_icon);
                    } else if (TextUtils.equals(Tags.Phone.M3_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m3_icon);
                    } else if (TextUtils.equals(Tags.Phone.MI_TV, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mtv_icon);
                    } else if (TextUtils.equals(Tags.Phone.MRED_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mred_icon);
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
        public String actId;
        public String productId;
        public String promotionType;
    }
}
