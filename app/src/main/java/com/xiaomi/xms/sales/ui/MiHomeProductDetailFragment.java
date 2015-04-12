
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ComboActivity;
import com.xiaomi.xms.sales.activity.FullScreenImageActivity;
import com.xiaomi.xms.sales.activity.MiHomeBuyActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.ProductDetailsAdapter;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader;
import com.xiaomi.xms.sales.loader.ProductDetailsLoader.Result;
import com.xiaomi.xms.sales.model.AddToShppingCartInfo;
import com.xiaomi.xms.sales.model.ProductDetailsInfo;
import com.xiaomi.xms.sales.model.ProductDetailsInfoItem;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.AddShoppingCartAnimation;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.PageScrollListener;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MiHomeProductDetailFragment extends BaseFragment implements
        LoaderCallbacks<ProductDetailsLoader.Result>, OnItemClickListener {

    private final static int PRODUCT_DETAILS_LOADER = 0;
    private final static int LOADING_MORE_STEP = 3;
    private EmptyLoadingView mLoadingView;
    private BaseListView mProductListView;
    private ProductDetailsAdapter mProductDetailsAdapter;
    private String mProductId;
    private String mGoodsId; // 货品ID，获取评价用
    private String mScannerId;
    private ProductDetailsInfo mProductDetailInfo;
    private AddToShppingCartInfo mToCart;
    private String mBuyCount;
    private int mClickLoadMoreCount;
    private ArrayList<ProductDetailsInfoItem> productDetailInfoList;
    private boolean mSetBottom;
    private String mMihomeId;

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
    private Button mShowProductBtn;
    private Button mShowCommentBtn;
    private ImageView mCartView;
    private CommentController mCommentController;
    private int mStorageCount;
    private AddShoppingCartAnimation mAddShoppingCartAnimation;
    private View mAddButtonLayout;
    private LinearLayout mLinearGotoCartView;

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
        mProductListView.addHeaderView(mHeadView, null, false);
        mProductListView.addFooterView(mFooterViewLoadingMore);
        mProductListView.addFooterView(mFooterView);
        mHeadView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
        mProductDetailsAdapter = new ProductDetailsAdapter(getActivity());
        mProductListView.setAdapter(mProductDetailsAdapter);
        mProductListView.setOnScrollListener(new PageScrollListener(null));
        mProductListView.setOnItemClickListener(this);
        getBundleData();
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
        mLoadingView.setEmptyText(R.string.product_detail_empty);
        mToCart = new AddToShppingCartInfo();
        mToCart.setProductId(mProductId);
        mCommentController = new CommentController(this, mGoodsId, mProductListView,
                mHeadView, mFooterViewLoadingMore, mShowCommentBtn, mShowProductBtn, mLoadingView);
        mCommentController.setOnShowProductListener(new CommentController.OnShowProductListener() {
            @Override
            public void onShow() {
                mProductListView.setAdapter(mProductDetailsAdapter);
                mProductListView.setOnItemClickListener(MiHomeProductDetailFragment.this);
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
        getActivity().setTitle(R.string.product_detail);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
        if (id == PRODUCT_DETAILS_LOADER && !TextUtils.isEmpty(mProductId)) {
            mLoader = new ProductDetailsLoader(getActivity());
            ((ProductDetailsLoader) mLoader).setProductId(mProductId);
            ((ProductDetailsLoader) mLoader).setMihomeId(mMihomeId);
            mLoader.setProgressNotifiable(mLoadingView);
            mLoader.setNeedDatabase(false);
            mClickLoadMoreCount = 1;
            return (Loader<Result>) mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mProductDetailInfo = data.mProductDetailsInfos;
        if (mProductDetailInfo == null) {
            mAddButtonLayout.setVisibility(View.GONE);
            mProductListView.setVisibility(View.GONE);
            return;
        }
        if (data.mMihomeStorageCount != 0) {
            mStorageCount = data.mMihomeStorageCount;
        }
        bindView();
        if (mSetBottom) {
            mProductListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            mSetBottom = false;
        }
        if (mProductDetailInfo == null) {
            Bundle bundel = new Bundle();
            bundel.putString(Constants.Intent.EXTRA_MIHOME_ERROR_RESULT, mScannerId);
            ((BaseActivity) getActivity()).showFragment(
                    MiHomeBuyActivity.TAG_MIHOME_BUY_ERROR_FRAGMENT, bundel, false);
        }
    }

    private void getBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mScannerId = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
            if (!TextUtils.isEmpty(mScannerId) && mScannerId.contains("_")) {
                mGoodsId = mScannerId.split("_")[0];
                mProductId = mScannerId.split("_")[1];
            } else {
                Bundle bundel = new Bundle();
                bundel.putString(Constants.Intent.EXTRA_MIHOME_ERROR_RESULT, mScannerId);
                ((BaseActivity) getActivity()).showFragment(
                        MiHomeBuyActivity.TAG_MIHOME_BUY_ERROR_FRAGMENT, bundel, false);
            }
            mMihomeId = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, "");
        }
    }

    public void bindView() {
        if (mProductDetailInfo != null) {
            mLinearAdaptList.removeAllViewsInLayout();
            mAddButtonLayout.setVisibility(View.VISIBLE);
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
        } else {
            mProductDetailsAdapter.updateData(null);
        }
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

    @Override
    public void onLoaderReset(Loader<Result> loader) {
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
        mHeadView.setVisibility(View.VISIBLE);
    }

    private void setFooterViewData() {
        mLoadingMore.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.VISIBLE);
        setSubmitView();
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.product_detail_submit:
                    respondSubmitClick();
                    break;
                case R.id.product_detail_loading_more:
                    loadingMore();
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
        if (LoginManager.getInstance().hasLogin()) {
            if (canNextStep()) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ComboActivity.class);
                intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, mProductId);
                intent.putExtra(Parameters.Keys.CONSUMPTION, mBuyCount);
                intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeId);
                startActivity(intent);
            }
            if (canAddShoppingCart()) {
                ((MiHomeBuyActivity) getActivity()).registerServiceAction();
                addShoppingCart();
            }
        } else {
            ToastUtil.show(getActivity(), R.string.login_before_op_shopping_cart);
            ((BaseActivity) getActivity()).gotoAccount();
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

    private void setSubmitView() {
        mSubmitView.setVisibility(View.VISIBLE);
        if (mStorageCount < 1) {
            setSumbitButtonAttribute(false, getString(R.string.none_stock));
        } else if (canAddShoppingCart()) {
            setSumbitButtonAttribute(true, getString(R.string.add_shopping_cart));
            mSubmitView.setOnClickListener(onClickListener);
        } else if (canNextStep()) {
            setSumbitButtonAttribute(true, getString(R.string.combo_choice_product));
            mSubmitView.setOnClickListener(onClickListener);
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
        intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mMihomeId);
        getActivity().startService(intent);
        setSumbitButtonAttribute(false, getString(R.string.doing_add_shopping_cart));
    }

    public void onAddShoppingCartFinish() {
        setSumbitButtonAttribute(true, getString(R.string.add_shopping_cart));
    }

    public void setSumbitButtonAttribute(boolean isClick, String text) {
        mSubmitView.setEnabled(isClick);
        mSubmitView.setText(text);
    }

    private class SpinnerItemListener implements OnItemSelectedListener {

        private ArrayList<String> styleList;

        public SpinnerItemListener(HashMap<String, String> map, ArrayList<String> styleList) {
            this.styleList = styleList;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (TextUtils.equals((String) parent.getTag(), getString(R.string.can_buy_max_count))) {
                mBuyCount = styleList.get(position);
            }
            mToCart.setConsumption(mBuyCount);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

    }

    @Override
    protected void onNetworkConnected(int type) {
        if (mLoader != null) {
            mLoader.reload();
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

    public void playAddCartAnimation() {
        mAddShoppingCartAnimation.setAnim();
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
