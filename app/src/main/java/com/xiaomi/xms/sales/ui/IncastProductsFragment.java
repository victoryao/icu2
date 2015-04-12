
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.ShoppingActivity;
import com.xiaomi.xms.sales.adapter.IncastProductGalleryAdapter;
import com.xiaomi.xms.sales.adapter.IncastProductPageAdapter;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode.IncastProduct;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;

import java.util.ArrayList;

public class IncastProductsFragment extends BaseFragment implements
        OnItemSelectedListener, OnClickListener, OnPageChangeListener {

    private ViewPager mPager;

    private IncastProductPageAdapter mPageAdapter;

    private Gallery mGallery;

    private IncastProductGalleryAdapter mGalleryAdapter;

    private TextView mNameInfo;

    private TextView mPriceInfo;

    private Button mAddButton;

    public boolean mIsHorizontalScrollAllowed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.incast_products_fragment, container,
                false);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        LayoutParams lp = mPager.getLayoutParams();
        lp.height = (int) (Device.DISPLAY_WIDTH - 2 * getActivity().getResources().getDimension(
                R.dimen.list_item_padding));
        mPager.setOnPageChangeListener(this);
        mPageAdapter = new IncastProductPageAdapter(getActivity());
        mPager.setAdapter(mPageAdapter);
        mGallery = (Gallery) view.findViewById(R.id.gallery);
        mGalleryAdapter = new IncastProductGalleryAdapter(
                getActivity());
        mGallery.setAdapter(mGalleryAdapter);
        Bundle bundle = getArguments();
        String json = bundle.getString(Constants.Intent.EXTRA_INCAST_PRODUCTS);
        ArrayList<IncastProduct> productList = IncastProduct.deserialize(json);
        mPageAdapter.updateData(productList);
        mGalleryAdapter.updateData(productList);
        mGallery.setOnItemSelectedListener(this);
        int productCount = productList.size();
        int middle = productCount / 2;
        if (middle * 2 == productCount && middle > 0) {
            middle--;
        }
        mGallery.setSelection(middle);
        mNameInfo = (TextView) view.findViewById(R.id.name_info);
        mPriceInfo = (TextView) view.findViewById(R.id.price_info);
        mAddButton = (Button) view.findViewById(R.id.add_button);
        mAddButton.setOnClickListener(this);
        setAddButtonEnabled(true);
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        mGalleryAdapter.setSelected(position);
        mGalleryAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(position);
        IncastProduct product = mGalleryAdapter.getData().get(position);
        mNameInfo.setText(product.getProductName());
        mPriceInfo.setText(ShopApp.getContext().getString(R.string.rmb_identification,
                product.getProductPrice()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.incast_product);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.add_button) {
            int pos = mGallery.getSelectedItemPosition();
            if (pos >= 0 && pos < mGallery.getCount()) {
                setAddButtonEnabled(false);
                IncastProduct product = mGalleryAdapter.getData().get(pos);
                ((ShoppingActivity) getActivity()).onAddPostFreeProduct(product);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position != mGalleryAdapter.getSelected()) {
            mGallery.setSelection(position);
        }
    }

    public void setAddButtonEnabled(boolean enabled) {
        if (enabled) {
            mAddButton.setEnabled(true);
            mAddButton.setText(R.string.add_shopping_cart);
        } else {
            mAddButton.setEnabled(false);
            mAddButton.setText(R.string.doing_add_shopping_cart);
        }
    }
}
