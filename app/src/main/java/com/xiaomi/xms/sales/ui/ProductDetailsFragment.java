
package com.xiaomi.xms.sales.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ComboActivity;
import com.xiaomi.xms.sales.activity.FullScreenImageActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.activity.SaleOutRegisterActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.ProductDetailsAdapter;
import com.xiaomi.xms.sales.loader.BaseLoader;
import com.xiaomi.xms.sales.loader.GetServiceNumberLoader;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader;
import com.xiaomi.xms.sales.loader.RecommendProductLoader;
import com.xiaomi.xms.sales.model.AddToShppingCartInfo;
import com.xiaomi.xms.sales.model.ProductDetailsInfo;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.nfc.NfcActivity;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.AddShoppingCartAnimation;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.ChooseStyleWindow;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class ProductDetailsFragment extends BaseFragment implements
        LoaderCallbacks<ProductDetailsLoader.Result>, OnItemClickListener {

    private static final int ADD_BUTTON_LAYOUT_HEIGHT;

    static {
        ADD_BUTTON_LAYOUT_HEIGHT = (int) ShopApp.getContext().getResources()
                .getDimension(R.dimen.add_button_layout_height);
    }
    private final static int PRODUCT_DETAILS_LOADER = 0;
    private final static int RECOMMEND_PRODUCT_LOADER = 1;
    private final static int LOADING_MORE_STEP = 3;
    private EmptyLoadingView mLoadingView;
    private BaseListView mProductListView;
    private ProductDetailsAdapter mProductDetailsAdapter;
    public String mProductId;
    public String mPid;
    private ProductDetailsInfo mProductDetailInfo;
    private AddToShppingCartInfo mToCart;
    private int mClickLoadMoreCount;
    private ArrayList<ProductDetailsInfoItem> productDetailInfoList;
    public String mNextId;
    public String mLastId;
    public boolean mNextIsPhone;
    public boolean mLastIsPhone;
    private int mPosition;
    private String mStyleId;

    private LinearLayout mHeadView;
    private View mFooterView;
    private LinearLayout mFooterViewLoadingMore;
    private View mAdaptPhoneContainer;
    private TextView mProductNameView;
    private TextView mProductPriceView;
    private TextView mProductMarketPriceView;
    private TextView mAdaptPhoneView;
    private Button mSubmitView;
    private Button mLoadingMore;
    private LinearLayout mLinearAdaptList;
    private LinearLayout mShowActivityView;
    private LinearLayout mLinearChoostList;
    private LinearLayout mLinearChoostSpinnerLayout;
    private LinearLayout mLinearGotoCartView;
    private OnAddFragmentListener mAddFragmentListener;
    private View mRecommendLabel;
    private LinearLayout mRecommendLayout;
    private BaseLoader<RecommendProductLoader.Result> mRecommendLoader;
    private Button mShowProductBtn;
    private Button mShowCommentBtn;
    private ImageView mCartView;
    private ImageView mStylePhoto;
    private CommentController mCommentController;
    private AddShoppingCartAnimation mAddShoppingCartAnimation;
    private View mAddButtonLayout;
    private PopupWindow mStyleWindow;
    private String mMihomeId;
    private int mStorageCount;
    private String mContainId;
    private boolean mIsWriteNfc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_details_fragment, container, false);
        mProductListView = (BaseListView) view.findViewById(android.R.id.list);
        mLinearGotoCartView = (LinearLayout) view.findViewById(R.id.goto_shoppingcart_layout);
        Button gotoBtn = (Button) view
                .findViewById(R.id.goto_shoppingcart_btn);
        gotoBtn.setOnClickListener(onClickListener);
        mAddButtonLayout = view.findViewById(R.id.add_shopping_layout);
        mSubmitView = (Button) view.findViewById(R.id.product_detail_submit);
        mCartView = (ImageView) view.findViewById(R.id.cart_icon);
        mHeadView = (LinearLayout) inflater.inflate(R.layout.product_detail_headview, null, false);
        mFooterView = inflater.inflate(R.layout.product_detail_footerview, null, false);
        mFooterViewLoadingMore = (LinearLayout) inflater.inflate(
                R.layout.product_detail_loading_more, null, false);
        initHeadView();
        initFooterView();
        initChooseWindow();
        mProductListView.addHeaderView(mHeadView, null, false);
        mProductListView.addFooterView(mFooterViewLoadingMore);
        mProductListView.addFooterView(mFooterView);
        mHeadView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
        mProductDetailsAdapter = new ProductDetailsAdapter(getActivity());
        mProductListView.setAdapter(mProductDetailsAdapter);
        mProductListView.setOnScrollListener(new PageScrollListener(null));
        mProductListView.setOnItemClickListener(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mProductId = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
            mPosition = bundle.getInt(Constants.Intent.EXTRA_PRODUCTVIEW_POSITION);
            mPid = bundle.getString(Constants.Intent.EXTRA_P_ID);
            mContainId = bundle.getString(Constants.Intent.EXTRA_CONTAIN_ID);
            mMihomeId = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, "");
            if (TextUtils.isEmpty(mPid)) {
                mPid = mProductId;
            }
            mIsWriteNfc = bundle.getBoolean(Constants.Intent.EXTRA_NFC_WRITE, false);
        } else {
            mProductId = "";
        }
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.product_detail_empty);
        mToCart = new AddToShppingCartInfo();
        mCommentController = new CommentController(this, mPid, mProductListView,
                mHeadView, mFooterViewLoadingMore, mShowCommentBtn, mShowProductBtn, mLoadingView);
        mCommentController.setOnShowProductListener(new CommentController.OnShowProductListener() {
            @Override
            public void onShow() {
                mProductListView.setAdapter(mProductDetailsAdapter);
                mProductListView.setOnItemClickListener(ProductDetailsFragment.this);
                mFooterViewLoadingMore.removeAllViews();
                mFooterViewLoadingMore.addView(mLoadingMore);
                setLoadingMore();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PRODUCT_DETAILS_LOADER, null, this);
        getLoaderManager().initLoader(RECOMMEND_PRODUCT_LOADER, null, mRecommendProductCallback);
        getActivity().setTitle(R.string.product_detail);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<ProductDetailsLoader.Result> onCreateLoader(int id, Bundle bundle) {
        if (id == PRODUCT_DETAILS_LOADER && !TextUtils.isEmpty(mProductId)) {
            mLoader = new ProductDetailsLoader(getActivity());
            ((ProductDetailsLoader) mLoader).setProductId(mProductId);
            if (!mIsWriteNfc) {
                ((ProductDetailsLoader) mLoader).setMihomeId(mMihomeId);
            }
            if (!TextUtils.isEmpty(mContainId)) {
                ((ProductDetailsLoader) mLoader).setContainId(mContainId);
            }
            mLoader.setProgressNotifiable(mLoadingView);
            mClickLoadMoreCount = 1;
            return (Loader<ProductDetailsLoader.Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ProductDetailsLoader.Result> loader,
            ProductDetailsLoader.Result data) {
        mProductDetailInfo = data.mProductDetailsInfos;
        if (mProductDetailInfo == null) {
            mStyleWindow.dismiss();
            mAddButtonLayout.setVisibility(View.GONE);
            mProductListView.setVisibility(View.GONE);
            return;
        }
        if (data.mMihomeStorageCount != 0) {
            mStorageCount = data.mMihomeStorageCount;
        }
        bindView();
        if (mProductDetailInfo != null) {
            mProductListView.setVisibility(View.VISIBLE);
            mAddButtonLayout.setVisibility(View.VISIBLE);
            mNextId = mProductDetailInfo.getNextItem();
            mLastId = mProductDetailInfo.getLastItem();
            mNextIsPhone = mProductDetailInfo.getNextIsPhone();
            mLastIsPhone = mProductDetailInfo.getLastIsPhone();
            if (mAddFragmentListener != null) {
                mAddFragmentListener.onAddFragmentItem(mLastId, mProductId, mNextId, mLastIsPhone,
                        mNextIsPhone, mPosition);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ProductDetailsLoader.Result> loader) {
    }

    private void bindView() {
        if (mProductDetailInfo != null) {
            mLinearAdaptList.removeAllViewsInLayout();
            mLinearChoostSpinnerLayout.removeAllViewsInLayout();
            mShowActivityView.removeAllViewsInLayout();

            productDetailInfoList = new ArrayList<ProductDetailsInfoItem>();

            if (mProductDetailInfo.getItems().size() > LOADING_MORE_STEP * mClickLoadMoreCount) {
                for (int i = 0; i < LOADING_MORE_STEP * mClickLoadMoreCount; i++) {
                    productDetailInfoList.add(mProductDetailInfo.getItems().get(i));
                }
                mProductDetailsAdapter.updateData(productDetailInfoList);
            } else {
                mProductDetailsAdapter.updateData(mProductDetailInfo.getItems());
            }
            setLoadingMore();
            setHeadViewData();
            setFooterViewData();
            setChooseData();
        } else {
            mProductDetailsAdapter.updateData(null);
        }
    }

    private void initHeadView() {
        mProductNameView = (TextView) mHeadView.findViewById(R.id.product_detail_name);
        mProductPriceView = (TextView) mHeadView.findViewById(R.id.product_detail_price);
        mProductMarketPriceView = (TextView) mHeadView
                .findViewById(R.id.product_detail_market_price);
        mProductMarketPriceView.getPaint().setFlags(
                Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        mAdaptPhoneContainer = mHeadView.findViewById(R.id.product_adapt_phone);
        mLinearAdaptList = (LinearLayout) mHeadView.findViewById(R.id.adapt_type_view);
        mAdaptPhoneView = (TextView) mHeadView.findViewById(R.id.adapt_phone_label);
        mShowProductBtn = (Button) mHeadView.findViewById(R.id.show_product_info_btn);
        mShowCommentBtn = (Button) mHeadView.findViewById(R.id.show_review_btn);
        mShowActivityView = (LinearLayout) mHeadView.findViewById(R.id.show_activity_container);
        mAdaptPhoneContainer.setVisibility(View.GONE);
    }

    private void initLoadingMore() {
        mLoadingMore = (Button) mFooterViewLoadingMore
                .findViewById(R.id.product_detail_loading_more);
        mLoadingMore.setOnClickListener(onClickListener);
        mLoadingMore.setEnabled(false);
        mLoadingMore.setVisibility(View.GONE);
    }

    private void initFooterView() {
        initLoadingMore();
        mRecommendLabel = mFooterView.findViewById(R.id.recommend_label);
        mRecommendLayout = (LinearLayout) mFooterView.findViewById(R.id.recommend_layout);
    }

    private void initChooseWindow() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        mLinearChoostList = (LinearLayout) layoutInflater.inflate(R.layout.choose_style_view, null);
        mStylePhoto = (ImageView) mLinearChoostList
                .findViewById(R.id.product_img);
        Button hideBtn = (Button) mLinearChoostList
                .findViewById(R.id.hide_window);
        mLinearChoostSpinnerLayout = (LinearLayout) mLinearChoostList
                .findViewById(R.id.choose_layout);
        hideBtn.setOnClickListener(onClickListener);
    }

    private void setHeadViewData() {
        mProductNameView.setText(mProductDetailInfo.getProductsName());
        mProductPriceView.setText(getString(R.string.rmb_identification,
                mProductDetailInfo.getProductPrice()));
        mProductMarketPriceView.setText(getString(R.string.rmb_identification,
                mProductDetailInfo.getProductMarketPrice()));
        if (!isSamePrice()) {
            mProductMarketPriceView.setVisibility(View.VISIBLE);
        } else {
            mProductMarketPriceView.setVisibility(View.GONE);
        }
        setAdaptPhoneView(mAdaptPhoneView, mAdaptPhoneContainer, mLinearAdaptList);
        setShowActivityView(mShowActivityView);
        mHeadView.setVisibility(View.VISIBLE);
    }

    private void setLoadingMore() {
        if (mProductDetailInfo.getItems().size() > LOADING_MORE_STEP * mClickLoadMoreCount) {
            mLoadingMore.setEnabled(true);
            mLoadingMore.setText(R.string.acquaintance_more);
        } else {
            mLoadingMore.setEnabled(false);
            mLoadingMore.setText("");
        }
    }

    private void setFooterViewData() {
        mLoadingMore.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.VISIBLE);
        setSubmitView();
    }

    private void setChooseData() {
        ImageLoader.getInstance().loadImage(mStylePhoto, mProductDetailInfo.getStylePhoto(),
                R.drawable.list_default_bg);
        setStyleNameView();
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.product_detail_submit:
                    if (!mStyleWindow.isShowing() && canAddShoppingCart()
                            && mProductDetailInfo.getStyleList().size() > 0) {
                        if (mProductDetailInfo.getStyleList().size() == 1
                                && mProductDetailInfo.getStyleList().get(0).getStyleDataMap()
                                        .size() == 1) {
                            respondSubmitClick();
                            return;
                        }
                        showStyleView();
                    } else {
                        respondSubmitClick();
                    }
                    break;
                case R.id.product_detail_loading_more:
                    loadingMore();
                    break;
                case R.id.hide_window:
                    hideStyleView();
                    break;
                case R.id.goto_shoppingcart_btn:
                    mLinearGotoCartView.setVisibility(View.GONE);
                    Intent intent = new Intent(getActivity(), ShoppingActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void loadingMore() {
        mClickLoadMoreCount++;
        int size = mProductDetailInfo.getItems().size();
        int start = LOADING_MORE_STEP * (mClickLoadMoreCount - 1);
        int max = mClickLoadMoreCount * LOADING_MORE_STEP;
        for (int i = start; i < (max <= size ? max : size); i++) {
            productDetailInfoList.add(mProductDetailInfo.getItems().get(i));
        }
        mProductDetailsAdapter.updateData(productDetailInfoList);
        if (max >= size) {
            mLoadingMore.setEnabled(false);
            mLoadingMore.setText("");
        }
    }

    private void respondSubmitClick() {
        if (mProductDetailInfo == null) {
            return;
        }
        if (LoginManager.getInstance().hasLogin()) {
            if (canNextStep()) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ComboActivity.class);
                intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
                intent.putExtra(Parameters.Keys.CONSUMPTION, "1");
                startActivity(intent);
            } else if (canAddShoppingCart()) {
                ((ProductDetailsActivity) getActivity()).registerServiceAction();
                hideStyleView();
                addShoppingCart();
                if (mSubmitView.getText().equals(getString(R.string.add_submit))) {
                }
            } else if (!mProductDetailInfo.hasProduct()) {
                Intent intent = new Intent(getActivity(), SaleOutRegisterActivity.class);
                intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
                startActivity(intent);
            }
        } else {
            ToastUtil
                    .show(getActivity(),
                            !mProductDetailInfo.hasProduct() ? R.string.login_before_register_sale_out_product
                                    : R.string.login_before_op_shopping_cart);
            ((BaseActivity) getActivity()).gotoAccount();
        }
    }

    private void updateStyleView(String styleId) {
        ((ProductDetailsLoader) mLoader).setProductId(styleId);
        mProductId = styleId;
        mLoader.forceLoad();
        mProductListView.setSelection(0);
    }

    private void setStyleNameView() {
        int rowCount = 1;
        int total = mProductDetailInfo.getStyleList().size();
        if (total > 0) {
            for (int i = 0; i < total; i = i + rowCount) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                FrameLayout styleSpinnerOne = (FrameLayout) layoutInflater.inflate(
                        R.layout.product_detail_spinner, null);
                final TextView tv = (TextView) styleSpinnerOne.findViewById(R.id.style_name);
                final CharSequence[] charSequences = getArray(i);
                final String styleType = getStyleType(i);
                final LinkedHashMap<String, String> styleMap = mProductDetailInfo.getStyleList()
                        .get(i).getStyleDataMap();
                mToCart.setProductId(mProductId);
                for (int j = 0; j < charSequences.length; j++) {
                    if (TextUtils.equals(styleMap.get(charSequences[j]), mProductId)) {
                        tv.setText(charSequences[j]);
                    }
                }
                styleSpinnerOne.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity());
                        builder.setTitle(styleType)
                                .setItems(charSequences, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mStyleId = styleMap.get(charSequences[which]);
                                        tv.setText(charSequences[which]);
                                        updateStyleView(mStyleId);
                                    }
                                });
                        AlertDialog dlg = builder.create();
                        dlg.show();
                    }
                });

                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, getResources()
                        .getDimensionPixelSize(
                                R.dimen.spinner_height));
                if (i != 0) {
                    params.topMargin = getResources().getDimensionPixelSize(
                            R.dimen.button_padding_size);
                }
                mLinearChoostSpinnerLayout.addView(styleSpinnerOne, params);
            }
        } else {
            mToCart.setProductId(mProductId);
        }

    }

    private CharSequence[] getArray(int index) {
        if (mProductDetailInfo.getStyleList().get(index) != null) {
            LinkedHashMap<String, String> map = mProductDetailInfo.getStyleList().get(index)
                    .getStyleDataMap();
            Set<String> set = map.keySet();
            CharSequence[] styleArray = set.toArray(new String[set.size()]);
            return styleArray;
        } else {
            return null;
        }
    }

    private String getStyleType(int index) {
        if (mProductDetailInfo.getStyleList().get(index) != null) {
            String type = mProductDetailInfo.getStyleList().get(index).getStyleType();
            return type;
        } else {
            return null;
        }
    }

    private void setAdaptPhoneView(TextView adaptphone, View adaptphoneContainer,
            LinearLayout adaptView) {
        if (mProductDetailInfo.getAdaptPhone() != null) {
            Set<String> set = mProductDetailInfo.getAdaptPhone().keySet();
            String[] adaptArray = set.toArray(new String[set.size()]);
            if (adaptArray != null) {
                adaptphone.setText(getString(R.string.adapt_phone));
                adaptphoneContainer.setVisibility(View.VISIBLE);
                for (int i = 0; i < adaptArray.length; i++) {
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    LinearLayout view = (LinearLayout) layoutInflater.inflate(
                            R.layout.adapt_phone_item, null);
                    View phoneType = view.findViewById(R.id.phone_type);
                    view.setTag(R.id.phone_type, phoneType);
                    TextView type = (TextView) view.getTag(R.id.phone_type);
                    type.setText(mProductDetailInfo.getAdaptPhone().get(adaptArray[i]));
                    if (TextUtils.equals(Tags.Phone.M11S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m11s_icon);
                    } else if (TextUtils.equals(Tags.Phone.M22S_PHONE, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.m22s_icon);
                    } else if (TextUtils.equals(Tags.Phone.MI_BOX, adaptArray[i])) {
                        type.setBackgroundResource(R.drawable.mbox_icon);
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
                    adaptView.addView(view);
                }
            }
        }
    }

    private void setShowActivityView(LinearLayout activityView) {
        if (mProductDetailInfo.getCanJoinActsList() != null) {
            for (int i = 0; i < mProductDetailInfo.getCanJoinActsList().size(); i++) {
                String title = mProductDetailInfo.getCanJoinActsList().get(i).getActsType();
                String description = mProductDetailInfo.getCanJoinActsList().get(i)
                        .getActsDescription();
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                LinearLayout view = (LinearLayout) layoutInflater.inflate(
                        R.layout.product_activity_item, null);
                TextView descriptionView = (TextView) view.findViewById(R.id.actvity_description);
                TextView typeView = (TextView) view.findViewById(R.id.actvity_type);
                typeView.setText(title);
                descriptionView.setText(description);
                activityView.addView(view);
            }
        }
    }

    private void setSubmitView() {
        mSubmitView.setVisibility(View.VISIBLE);
        if (mIsWriteNfc) {
            mSubmitView.setBackgroundResource(R.drawable.btn_primary_bg);
            mSubmitView.setTextColor(ShopApp.getContext().getResources()
                    .getColor(R.color.primary_text_inverse));
            setSumbitButtonAttribute(true, getString(R.string.nfc_product_write_button_info));
            mSubmitView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NfcActivity.class);
                    intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
                    getActivity().startActivity(intent);
                }
            });
            return;
        }
        mSubmitView.setOnClickListener(onClickListener);
        if (mStorageCount < 1) {
            mSubmitView.setBackgroundResource(R.drawable.btn_secondary_bg);
            mSubmitView.setTextColor(ShopApp.getContext().getResources()
                    .getColor(R.color.primary_text));
            setSumbitButtonAttribute(false, getString(R.string.none_stock));
        } else {
            mSubmitView.setBackgroundResource(R.drawable.btn_primary_bg);
            mSubmitView.setTextColor(ShopApp.getContext().getResources()
                    .getColor(R.color.primary_text_inverse));
            if (canAddShoppingCart()) {
                setSumbitButtonAttribute(true, getString(R.string.add_shopping_cart));
            } else if (canNextStep()) {
                setSumbitButtonAttribute(true, getString(R.string.combo_choice_product));
            }
        }
    }

    private boolean canNextStep() {
        return mStorageCount >= 1 && mProductDetailInfo.isChoiceCombo();
    }

    private boolean canAddShoppingCart() {
        return mStorageCount >= 1 && !mProductDetailInfo.isChoiceCombo();
    }

    private void addShoppingCart() {
		Intent intent = new Intent(getActivity(), ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_ADD_SHOPPING_CART);
        intent.putExtra(Parameters.Keys.PRODUCT_ID, mToCart.mProductId);
        intent.putExtra(Parameters.Keys.CONSUMPTION, mToCart.mConsumption);
        intent.putExtra(Parameters.Keys.PROMOTION_ID, mToCart.mPromotionId);
        intent.putExtra(Parameters.Keys.PROMOTION_TYPE, mToCart.mPromotionType);
        intent.putExtra(Parameters.Keys.SECURITY_CODE, mToCart.mSecurityCode);
        getActivity().startService(intent);
	   
    }

    public void onAddShoppingCartFinish() {
        setSumbitButtonAttribute(true, getString(R.string.add_shopping_cart));
    }

    private void setSumbitButtonAttribute(boolean isClick, String text) {
        mSubmitView.setEnabled(isClick);
        mSubmitView.setText(text);
    }

    @Override
    protected void onNetworkConnected(int type) {
        if (mLoader != null) {
            mLoader.reload();
        }
        if (mRecommendLoader != null) {
            mRecommendLoader.reload();
        }
    }

    private boolean isSamePrice() {
        if (!TextUtils.equals(mProductDetailInfo.getProductPrice(),
                mProductDetailInfo.getProductMarketPrice())) {
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adpterView, View view, int position, long id) {
        if (position < mProductListView.getHeaderViewsCount()
                || position >= mProductListView.getCount() - mProductListView.getFooterViewsCount()) {
            return;
        }

        Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
        intent.putExtra(Constants.Intent.EXTRA_GO_TO_FRAGMENT,
                FullScreenImageActivity.TAG_PRODUCT_DETAIL_FULL_SCREEN_FRAGMENT);
        intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
        intent.putExtra(Constants.Intent.EXTRA_FULL_SCREEN_START_INDEX,
                position - mProductListView.getHeaderViewsCount());
        startActivity(intent);
    }

    public interface OnAddFragmentListener {
        public void onAddFragmentItem(String lastId, String thisId, String nextId,
                boolean lastIsPhone, boolean nextIsPhone, int postion);
    }

    public void setOnAddFragmentListener(OnAddFragmentListener l) {
        mAddFragmentListener = l;
    }

    public void moveAddFragmentListener() {
        mAddFragmentListener = null;
    }

    private LoaderCallbacks<RecommendProductLoader.Result> mRecommendProductCallback =
            new LoaderCallbacks<RecommendProductLoader.Result>() {
                @Override
                public Loader<RecommendProductLoader.Result> onCreateLoader(int id, Bundle bundle) {
                    if (id == RECOMMEND_PRODUCT_LOADER) {
                        mRecommendLoader = new RecommendProductLoader(getActivity(), mProductId);
                        return mRecommendLoader;
                    }
                    return null;
                }

                @Override
                public void onLoadFinished(Loader<RecommendProductLoader.Result> loader,
                        RecommendProductLoader.Result result) {
                    mRecommendLayout.removeAllViews();
                    if (result != null) {
                        ArrayList<ProductInfo> recommendProducts = result.mRecommandProducts;
                        if (recommendProducts != null) {
                            Context context = getActivity();
                            LayoutInflater inflater = (LayoutInflater) context
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            int size = recommendProducts.size();
                            for (int i = 0; i < size; ++i) {
                                ProductInfo one = recommendProducts.get(i);
                                View view = inflater.inflate(R.layout.recommend_item,
                                        mRecommendLayout, false);
                                RecommendProductInfo tag = new RecommendProductInfo(one, i);
                                view.setTag(tag);
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RecommendProductInfo tag = (RecommendProductInfo) v
                                                .getTag();
                                        ProductInfo info = tag.info;
                                        // 本地应用打开
                                        Intent intent = new Intent();
                                        intent.setClass(getActivity(), ProductDetailsActivity.class);
                                        intent.putExtra(
                                                Constants.Intent.EXTRA_PRODUCT_ID,
                                                info.getProductId().split("_").length > 3 ? info.getProductId().split(
                                                        "_")[2] : info.getProductId());
                                        if (!TextUtils.isEmpty(info.getUrl())) {
                                            intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
                                            intent.putExtra(Constants.Intent.EXTRA_MIPHONE_NAME,
                                                    info.getProductName());
                                        }
                                        ProductDetailsFragment.this.getActivity().startActivity(
                                                intent);
                                    }
                                });
                                ImageView iv = (ImageView) view
                                        .findViewById(R.id.recommend_item_image);
                                TextView tv = (TextView) view
                                        .findViewById(R.id.recommend_item_price);
                                ImageLoader.getInstance().loadImage(iv, one.getImage(),
                                        R.drawable.default_pic_large);
                                tv.setText(getString(R.string.rmb_identification,
                                        one.getProductPrice()));
                                mRecommendLayout.addView(view);
                            }
                            if (size != 0) {
                                mRecommendLabel.setVisibility(View.VISIBLE);
                                mRecommendLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<RecommendProductLoader.Result> arg0) {
                }
            };

    public static class RecommendProductInfo {
        public ProductInfo info;
        public int position;

        public RecommendProductInfo(ProductInfo info, int position) {
            this.info = info;
            this.position = position;
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        if (isVisible() && mRecommendLoader != null) {
            mRecommendLoader.reload();
        }
    }

    public void playAddCartAnimation() {
        mAddShoppingCartAnimation.setAnim();
    }

    private void showStyleView() {
        mStyleWindow.showAtLocation(mAddButtonLayout, Gravity.NO_GRAVITY, 0, 0);
        setSumbitButtonAttribute(true, getString(R.string.add_submit));
    }

    public void hideStyleView() {
        mStyleWindow.dismiss();
        setSubmitView();
    }

    public boolean styleWindowStatus() {
        if (mStyleWindow.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    public void showGotoCartWindow() {
        mLinearGotoCartView.setVisibility(View.VISIBLE);
        mLinearGotoCartView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.appear_from_title));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                try {
                    mLinearGotoCartView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.disappear_from_title));
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            hideGotoCartWindow();
                        }
                    }, 500);
                } catch (Exception e) {

                }
            }
        }, 2000);
    }

    public void hideGotoCartWindow() {
        mLinearGotoCartView.setVisibility(View.GONE);
    }

}
