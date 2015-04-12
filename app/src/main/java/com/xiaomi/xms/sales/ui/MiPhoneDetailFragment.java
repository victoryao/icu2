
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.CampaignActivity;
import com.xiaomi.xms.sales.activity.FCodeActivity;
import com.xiaomi.xms.sales.activity.ProductDetailsActivity;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.MiPhoneLoader;
import com.xiaomi.xms.sales.loader.MiPhoneLoader.Result;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.FeatureItem;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.MediaItem;
import com.xiaomi.xms.sales.model.MiPhoneDetailInfo.Item.RecommendItem;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.widget.SelfBindView;
import com.xiaomi.xms.sales.widget.SelfBindView.SelfBindViewInteface;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

import java.util.ArrayList;

public class MiPhoneDetailFragment extends BaseFragment implements
        LoaderCallbacks<MiPhoneLoader.Result> {

    public static final int MIPHONE_DETAIL_LOADER = 0;
    public static final int MEDIA_HORIZONTAL_COUNT = 3;
    public static final String DEFAULT_PHONE_TYPE = "0";
    private EmptyLoadingView mLoadingView;
    public String mProductId;
    private MiPhoneDetailInfo mMiPhoneDetailInfo;
    private LinearLayout mLinearHead;
    private LinearLayout mLinearMedia;
    private LinearLayout mLinearSecondMedia;
    private LinearLayout mLinearFeature;
    private LinearLayout mLinearGallary;
    private LinearLayout mLinearCurMiPhone;
    private LinearLayout mLinearLoadMore;
    private LinearLayout mInterested;
    private LinearLayout mPlaceholderView;
    private OnAddPhoneFragmentListener mAddPhoneFragmentListener;
    public String mNextId;
    public String mLastId;
    public boolean mNextIsPhone;
    public boolean mLastIsPhone;
    private int mPosition;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        mProductId = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
        mPosition = bundle.getInt(Constants.Intent.EXTRA_PRODUCTVIEW_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.miphone_detail_fragment, container, false);
        mLinearHead = (LinearLayout) view.findViewById(R.id.miphone_head_container);
        mLinearMedia = (LinearLayout) view.findViewById(R.id.miphone_media_container);
        mLinearSecondMedia = (LinearLayout) view.findViewById(R.id.miphone_media_container_second);
        mLinearFeature = (LinearLayout) view.findViewById(R.id.miphone_feature_container);
        mLinearGallary = (LinearLayout) view.findViewById(R.id.miphone_recommend_container);
        mLinearCurMiPhone = (LinearLayout) view.findViewById(R.id.miphone_footer_container);
        mLinearLoadMore = (LinearLayout) view.findViewById(R.id.miphone_load_more_container);
        mInterested = (LinearLayout) view.findViewById(R.id.maybe_interested_container);
        mPlaceholderView = (LinearLayout) view.findViewById(R.id.placeholder_view);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loadingview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MIPHONE_DETAIL_LOADER, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == MIPHONE_DETAIL_LOADER) {
            mLoader = new MiPhoneLoader(getActivity());
            ((MiPhoneLoader) mLoader).setProductId(mProductId);
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mMiPhoneDetailInfo = data.mMiPhoneDetailInfos;
        bindHeadView();
        setData();
        if (mMiPhoneDetailInfo != null) {
            mNextId = mMiPhoneDetailInfo.getNextItem();
            mLastId = mMiPhoneDetailInfo.getLastItem();
            mNextIsPhone = mMiPhoneDetailInfo.getNextIsPhone();
            mLastIsPhone = mMiPhoneDetailInfo.getLastIsPhone();
            if (mAddPhoneFragmentListener != null) {
                mAddPhoneFragmentListener.onAddFragmentItem(mLastId, mProductId, mNextId,
                        mLastIsPhone, mNextIsPhone, mPosition);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    private void bindHeadView() {
        mLinearHead.removeAllViewsInLayout();
        if (mMiPhoneDetailInfo != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            final LinearLayout parentView = (LinearLayout) layoutInflater.inflate(
                    R.layout.miphone_detail_headview, null);
            SelfBindView headImg = (SelfBindView) parentView.findViewById(R.id.head_img);
            headImg.SelfBindViewCallBack = new SelfBindViewInteface() {
                @Override
                public void bindView(ImageView view, Bitmap bitmap, Image image) {
                    parentView.setLayoutParams(new LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT, bitmap.getHeight()
                                    * Device.DISPLAY_WIDTH / bitmap.getWidth()));
                    view.setImageBitmap(image.proccessImage(bitmap));
                }
            };
            ImageLoader.getInstance().loadImage(headImg, mMiPhoneDetailInfo.getFocusImg(),
                    R.drawable.default_pic_large);
            mLinearHead.addView(parentView);
        }
    }

    private void setData() {
        mLinearMedia.removeAllViewsInLayout();
        mLinearSecondMedia.removeAllViewsInLayout();
        mLinearFeature.removeAllViewsInLayout();
        mLinearGallary.removeAllViewsInLayout();
        mLinearCurMiPhone.removeAllViewsInLayout();

        if (mMiPhoneDetailInfo != null) {
            ArrayList<MediaItem> list = new ArrayList<MediaItem>();
            for (int i = 0; i < mMiPhoneDetailInfo.getItem().size(); i++) {
                Item data = mMiPhoneDetailInfo.getItem().get(i);
                if (data.getType() == Item.TYPE_MEDIA) {
                    list.add((MediaItem) data.getNode());
                } else if (data.getType() == Item.TYPE_FEATURES) {
                    bindFeatureView((FeatureItem) data.getNode());
                } else if (data.getType() == Item.TYPE_GALLERY) {
                    bindRecommendView((RecommendItem) data.getNode());
                }
            }
            if (list != null) {
                bindMediaView(list);
            }
            mLinearLoadMore.setVisibility(View.VISIBLE);
            mInterested.setVisibility(View.VISIBLE);
            mPlaceholderView.setVisibility(View.VISIBLE);
        }
    }

    private void bindMediaView(ArrayList<MediaItem> data) {
        int total = data.size();
        int step = 1;
        for (int i = 0; i < total; i = i + step) {
            if (total == MEDIA_HORIZONTAL_COUNT) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                LinearLayout view = (LinearLayout) layoutInflater.inflate(
                        R.layout.miphone_media_item, null);
                TextView introduce = (TextView) view.findViewById(R.id.introduce);
                ImageView show = (ImageView) view.findViewById(R.id.media_img);
                introduce.setText(data.get(i).getMediaItemText());
                ImageLoader.getInstance().loadImage(show, data.get(i).getMediaItemImg(),
                        R.drawable.default_pic_small_inverse);
                final String url = data.get(i).getMediaItemUrl();
                show.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openBrowserToMedia(url);
                    }
                });
                LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                if (i == total - 1) {
                    params.rightMargin = getResources().getDimensionPixelSize(
                            R.dimen.button_padding_size);
                }
                params.leftMargin = getResources().getDimensionPixelSize(
                        R.dimen.button_padding_size);
                mLinearMedia.addView(view, params);
            } else {
                step = 2;
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                LinearLayout view = (LinearLayout) layoutInflater.inflate(
                        R.layout.miphone_media_second_itme, null);
                LinearLayout secondItemView = (LinearLayout) view
                        .findViewById(R.id.media_second_item);
                secondItemView.setVisibility(View.INVISIBLE);
                TextView introduce = (TextView) view.findViewById(R.id.media_introduce);
                ImageView show = (ImageView) view.findViewById(R.id.media_image);
                introduce.setText(data.get(i).getMediaItemText());
                ImageLoader.getInstance().loadImage(show, data.get(i).getMediaItemImg(),
                        R.drawable.default_pic_small_inverse);
                final String url = data.get(i).getMediaItemUrl();
                show.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openBrowserToMedia(url);
                    }
                });
                if (i + 1 < total) {
                    secondItemView.setVisibility(View.VISIBLE);
                    TextView secondIntroduce = (TextView) view
                            .findViewById(R.id.media_introduce_second);
                    ImageView secondShow = (ImageView) view.findViewById(R.id.media_image_second);
                    secondIntroduce.setText(data.get(i + 1).getMediaItemText());
                    ImageLoader.getInstance()
                            .loadImage(secondShow, data.get(i + 1).getMediaItemImg(),
                                    R.drawable.default_pic_small_inverse);
                    final String secondUrl = data.get(i + 1).getMediaItemUrl();
                    secondShow.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openBrowserToMedia(secondUrl);
                        }
                    });
                }
                mLinearSecondMedia.addView(view);
            }

        }
    }

    private void bindFeatureView(FeatureItem data) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final LinearLayout parentView = (LinearLayout) layoutInflater.inflate(R.layout.selfbind_container,
                null);
        SelfBindView mShow = (SelfBindView) parentView.findViewById(R.id.selfbind_image);
        mShow.SelfBindViewCallBack = new SelfBindViewInteface() {
            @Override
            public void bindView(ImageView view, Bitmap bitmap, Image image) {
                parentView.setLayoutParams(new LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, bitmap.getHeight()
                                * Device.DISPLAY_WIDTH / bitmap.getWidth()));
                view.setImageBitmap(image.proccessImage(bitmap));
            }
        };
        ImageLoader.getInstance().loadImage(mShow, data.getFeatureItemImg(),
                R.drawable.default_pic_large);
        mLinearFeature.addView(parentView);
    }

    private void bindRecommendView(RecommendItem data) {
        if (TextUtils.equals(mProductId, data.getProductId())) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            LinearLayout view = (LinearLayout) layoutInflater
                    .inflate(R.layout.miphone_footer, null);
            TextView phoneName = (TextView) view.findViewById(R.id.phone_name_foot);
            TextView phonePrice = (TextView) view.findViewById(R.id.phone_price_foot);
            Button buy = (Button) view.findViewById(R.id.buy_miphone);
            Button codeBuy = (Button) view.findViewById(R.id.fcode_buy_miphone);
            phoneName.setText(data.getProductName());
            phonePrice.setText(getResources().getString(R.string.rmb_identification,
                    data.getProductPrice()));
            codeBuy.setText(getString(R.string.fcode_buy));
            if (data.getIsCanBuy()) {
                buy.setText(getString(R.string.immediately_buy));
            } else {
                buy.setText(getString(R.string.miphone_none_stock));
                buy.setEnabled(false);
            }
            buy.setOnClickListener(onClickListener);
            codeBuy.setOnClickListener(onClickListener);
            mLinearCurMiPhone.addView(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            LinearLayout view = (LinearLayout) layoutInflater.inflate(
                    R.layout.miphone_recommend_item, null);
            ImageView phoneImg = (ImageView) view.findViewById(R.id.gallery_img);
            TextView phoneName = (TextView) view.findViewById(R.id.phone_name);
            TextView phonePrice = (TextView) view.findViewById(R.id.phone_price);
            TextView phoneBrief = (TextView) view.findViewById(R.id.phone_brief);
            ImageLoader.getInstance().loadImage(phoneImg, data.getRecommendItemImg(),
                    R.drawable.list_default_bg);
            phoneName.setText(data.getProductName());
            phonePrice.setText(getResources().getString(R.string.rmb_identification,
                    data.getProductPrice()));
            phoneBrief.setText(data.getProductBrief());
            final String productId = data.getProductId();
            final int isPhone = data.isPhone();
            final String activityUrl = data.getActivityUrl();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToRecommendProduct(productId, isPhone, activityUrl);
                }
            });
            mLinearGallary.addView(view);
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buy_miphone:
                    immediatelyBuyMiPhone();
                    break;
                case R.id.fcode_buy_miphone:
                    fCodeBuyMiPhone();
                    break;
            }
        }
    };

    private void immediatelyBuyMiPhone() {
        if (LoginManager.getInstance().hasLogin()) {
            String phoneType = mMiPhoneDetailInfo.getPhoneType();
            phoneType = TextUtils.isEmpty(phoneType) ? DEFAULT_PHONE_TYPE : phoneType;
            String url = HostManager.URL_XIAOMI_SHOP_MIPHONE_IMMEDIATELY_BUY_URL
                    + phoneType;
            LogUtil.d("immediatelyBuyMiPhone", url);
            CampaignActivity.startActivityStandard((BaseActivity) getActivity(), url);
        } else {
            ToastUtil.show(getActivity(), getString(R.string.please_login));
            ((BaseActivity) getActivity()).gotoAccount();
        }
    }

    private void fCodeBuyMiPhone() {
        if (LoginManager.getInstance().hasLogin()) {
            Intent intent = new Intent(getActivity(), FCodeActivity.class);
            startActivity(intent);
        } else {
            ToastUtil.show(getActivity(), getString(R.string.fcode_buy_please_login));
            ((BaseActivity) getActivity()).gotoAccount();
        }
    }

    private void openBrowserToMedia(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        if (!TextUtils.isEmpty(url)) {
            startActivity(intent);
        }
    }

    private void goToRecommendProduct(String phoneId, int isphone, String activityUrl) {
        if (!TextUtils.isEmpty(activityUrl)) {
            CampaignActivity.startActivityStandard((BaseActivity) getActivity(), activityUrl);
            return;
        }
        if (!TextUtils.isEmpty(phoneId)) {
            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
            if (isphone == 1) {
                intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, true);
            } else if (isphone == 0) {
                intent.putExtra(Constants.Intent.EXTRA_IS_MIPHONE, false);
            }
            intent.putExtra(Constants.Intent.EXTRA_PRODUCT_ID, phoneId);
            getActivity().startActivity(intent);
        }
    }

    public interface OnAddPhoneFragmentListener {
        public void onAddFragmentItem(String lastId, String thisId, String nextId,
                boolean lastIsPhone, boolean nextIsPhone, int position);
    }

    public void setOnAddPhoneFragmentListener(OnAddPhoneFragmentListener l) {
        mAddPhoneFragmentListener = l;
    }

    public void moveAddPhoneFragmentListener() {
        mAddPhoneFragmentListener = null;
    }

}
