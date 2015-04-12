
package com.xiaomi.xms.sales.ui;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.ComboActivity;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.BaseSpinnerAdapter;
import com.xiaomi.xms.sales.loader.ComboLoader;
import com.xiaomi.xms.sales.loader.ComboLoader.Result;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.AddToShppingCartInfo;
import com.xiaomi.xms.sales.model.ComboInfo;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.request.HostManager.Parameters;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.widget.AddShoppingCartAnimation;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ComboFragment extends BaseFragment implements LoaderCallbacks<ComboLoader.Result> {
    private final static String TAG = "ComboFragment";
    private final static int COMBO_LOADER = 0;
    private final static int TAG_POSITION = -1;
    private EmptyLoadingView mLoadingView;
    private String mComboId;
    private LinearLayout mLinearList;
    private ComboInfo mInfo;
    private AddToShppingCartInfo mToCart;
    private String mComboCount;
    private ArrayList<String> mProductList;
    private AddShoppingCartAnimation mAddShoppingCartAnimation;
    private Button mAddCartSubmit;
    private View mAddButtonLayout;
    private ImageView mCartView;
    private LinearLayout mLinearGotoCartView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        mComboId = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
        mComboCount = bundle.getString(Parameters.Keys.CONSUMPTION);
        getActivity().setTitle(R.string.combo_detail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.combo_fragment, container, false);
        mLinearList = (LinearLayout) view.findViewById(R.id.combo_list_container);
        mLinearGotoCartView = (LinearLayout) view.findViewById(R.id.goto_shoppingcart_layout);
        Button gotoBtn = (Button) view
                .findViewById(R.id.goto_shoppingcart_btn);
        gotoBtn.setOnClickListener(mClickListener);
        mAddButtonLayout = view.findViewById(R.id.add_shopping_layout);
        mAddCartSubmit = (Button) view.findViewById(R.id.combo_detail_submit);
        mAddCartSubmit.setText(R.string.add_shopping_cart);
        mAddCartSubmit.setOnClickListener(mClickListener);
        mCartView = (ImageView) view.findViewById(R.id.cart_icon);
        mToCart = new AddToShppingCartInfo();
        mToCart.setProductId(mComboId);
        mToCart.setConsumption(mComboCount);
        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loadingview);
        getLoaderManager().initLoader(COMBO_LOADER, null, this);
      
        return view;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == COMBO_LOADER) {
            mLoader = new ComboLoader(getActivity());
            ((ComboLoader) mLoader).setProductId(mComboId);
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mLinearList.removeAllViewsInLayout();
        mInfo = data.mComboInfos;
        if (mInfo != null) {
            mAddButtonLayout.setVisibility(View.VISIBLE);
            mProductList = new ArrayList<String>();
            setViewData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Result> arg0) {
    }

    public LinearLayout initItemView() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.combo_list_item, null);
        View comboProductPhoto = view.findViewById(R.id.combo_product_photo);
        View comboProductName = view.findViewById(R.id.combo_product_name);
        View comboProductStyle = view.findViewById(R.id.combo_choose_style);
        comboProductStyle.setTag(R.id.combo_product_name, comboProductName);
        comboProductStyle.setTag(R.id.combo_product_photo, comboProductPhoto);
        return view;
    }

    private void setViewData() {
        for (int i = 0; i < mInfo.getSelectedProducts().size(); i++) {
            String mPriductId = mInfo.getSelectedProducts().get(i).getProductId();
            View viewItem = initItemView();
            Spinner comboProductStyle = (Spinner) viewItem.findViewById(R.id.combo_choose_style);
            ImageView comboProductPhoto = (ImageView) viewItem
                    .findViewById(R.id.combo_product_photo);
            TextView comboProductName = (TextView) viewItem
                    .findViewById(R.id.combo_product_name);
            comboProductStyle.setTag(TAG_POSITION, i);

            if (mInfo.getComboProductList().get(i).isEmpty()) {
                comboProductStyle.setVisibility(View.GONE);
                ImageLoader.getInstance()
                        .loadImage(comboProductPhoto,
                                mInfo.getSelectedProducts().get(i).getImage(),
                                R.drawable.default_pic_small);
                comboProductName.setText(mInfo.getSelectedProducts().get(i).getProductName());
            } else {
                Set<String> set = mInfo.getComboProductList().get(i).keySet();
                String[] styleArray = set.toArray(new String[set.size()]);
                HashMap<String, ProductInfo> comboMap = mInfo.getComboProductList().get(i);
                ArrayList<String> styleList = new ArrayList<String>();
                for (int j = 0; j < styleArray.length; j++) {
                    if (TextUtils.equals(mPriductId, comboMap.get(styleArray[j]).getProductId())) {
                        styleList.add(0, styleArray[j]);
                    } else {
                        styleList.add(styleArray[j]);
                    }
                }
                if (styleList.size() == 1) {
                    comboProductStyle.setVisibility(View.GONE);
                    ImageLoader.getInstance()
                            .loadImage(comboProductPhoto,
                                    comboMap.get(styleList.get(0)).getImage(),
                                    R.drawable.default_pic_small);
                    comboProductName.setText(comboMap.get(styleList.get(0)).getProductName());
                    mPriductId = comboMap.get(styleList.get(0)).getProductId();
                } else {
                    comboProductStyle.setVisibility(View.VISIBLE);
                    BaseSpinnerAdapter adapter = new BaseSpinnerAdapter(getActivity());
                    comboProductStyle.setAdapter(adapter);
                    comboProductStyle.setPrompt(getString(R.string.please_choose));
                    adapter.updateData(styleList);
                    comboProductStyle.setOnItemSelectedListener(new SpinnerItemListener(comboMap,
                            styleList));
                }
            }
            setViewItemBackground(viewItem, mInfo.getSelectedProducts().size(), i);
            mLinearList.addView(viewItem);
            mProductList.add(mPriductId);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View addCartSubmitLayout = layoutInflater.inflate(R.layout.combo_addcart_button, null);
        mLinearList.addView(addCartSubmitLayout);
    }

    private void setViewItemBackground(View view, int total, int position) {
        // 如果仅有一条
        if (total == 1) {
            view.setBackgroundResource(R.drawable.list_item_single_bg_n);
        } else if (position == 0) {
            view.setBackgroundResource(R.drawable.list_item_top_bg_n);
        } else if (position == total - 1) {
            view.setBackgroundResource(R.drawable.list_item_bottom_bg_n);
        } else {
            view.setBackgroundResource(R.drawable.list_item_middle_bg_n);
        }
    }

    private class SpinnerItemListener implements OnItemSelectedListener {
        private ArrayList<String> styleList;
        private HashMap<String, ProductInfo> map;

        public SpinnerItemListener(HashMap<String, ProductInfo> map, ArrayList<String> styleList) {
            this.styleList = styleList;
            this.map = map;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int j = (Integer) parent.getTag(TAG_POSITION);
            ProductInfo info = map.get(styleList.get(position));
            ImageLoader.getInstance().loadImage(
                    (ImageView) parent.getTag(R.id.combo_product_photo),
                    info.getImage(), R.drawable.default_pic_small);
            ((TextView) parent.getTag(R.id.combo_product_name)).setText(info
                    .getProductName());
            mProductList.set(j, info.getProductId());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.combo_detail_submit:
                    mToCart.setAllProductId(TextUtils.join("|", mProductList));
                    ((ComboActivity) getActivity()).registerServiceAction();
                    addShoppingCart();
                    break;
                case R.id.goto_shoppingcart_btn:
                    mLinearGotoCartView.setVisibility(View.GONE);
                    Intent intent = new Intent(getActivity(), ShoppingActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void addShoppingCart() {
        Intent intent = new Intent(getActivity(), ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_ADD_SHOPPING_CART);
        intent.putExtra(Parameters.Keys.ITEM_IDS, mToCart.mAllProductId);
        intent.putExtra(Parameters.Keys.PRODUCT_ID, mToCart.mProductId);
        intent.putExtra(Parameters.Keys.CONSUMPTION, mToCart.mConsumption);
        intent.putExtra(Parameters.Keys.PROMOTION_ID, mToCart.mPromotionId);
        intent.putExtra(Parameters.Keys.PROMOTION_TYPE, mToCart.mPromotionType);
        intent.putExtra(Parameters.Keys.SECURITY_CODE, mToCart.mSecurityCode);
        getActivity().startService(intent);
        setSumbitButtonAttribute(false, getString(R.string.doing_add_shopping_cart));
    }

    public void onAddShoppingCartFinish() {
        setSumbitButtonAttribute(true, getString(R.string.add_shopping_cart));
    }

    public void setSumbitButtonAttribute(boolean isClick, String text) {
        mAddCartSubmit.setEnabled(isClick);
        mAddCartSubmit.setText(text);
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
