package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.SameDayExchangeActivity;
import com.xiaomi.xms.sales.activity.XianhuoOrderActivity;
import com.xiaomi.xms.sales.adapter.ExchangeOrderViewProductAdapter;
import com.xiaomi.xms.sales.loader.ExchangeReplaceLoader;
import com.xiaomi.xms.sales.loader.OrderInfoLoader;
import com.xiaomi.xms.sales.model.Order.ProductBrief;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

public class ExchangeListFragment extends BaseFragment implements OnItemClickListener{

	private static final String TAG = "ExchangeListFragment";
	private static final int EXCHANGE_REPLACE_LOADER = 0;
	private Button mPayButton;
	private BaseListView mListView;
	private View mListFooterView;
	private View mActionContainer;
	private OrderInfoLoader mLoader;
	private ExchangeOrderViewProductAdapter mAdapter;
	private EmptyLoadingView mLoadingView;
	private StringBuilder mReplaceSn = new StringBuilder();
	private Bundle mBundle;
	private String srcSn;
	private String destSn;

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.exchange_list_fragment, container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer, null, false);
		mListFooterView.setVisibility(View.VISIBLE);
		mListView.addFooterView(mListFooterView);
		mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0, getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding), getResources()
				.getDimensionPixelSize(R.dimen.list_item_padding));
		mActionContainer = view.findViewById(R.id.action_container);
		mListView.setOnItemClickListener(this);
		mPayButton = (Button) view.findViewById(R.id.choice_confirm_btn);
		handleIntent();

		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.order_list_empty);
		return view;
	}

	private void handleIntent() {
		mBundle = getArguments();
		if (mBundle != null) {
			destSn = mBundle.getString(Constants.Intent.EXTRA_EXCHANGE_DEST_SN_STR);
			srcSn = mBundle.getString(Constants.Intent.EXTRA_EXCHANGE_SRC_SN_STR);
			if(srcSn != null && destSn != null){
				initProductSnList();
			}
		}else{
			mBundle = new Bundle();
		}
	}

	private void initProductSnList() {
		ArrayList<ProductBrief> productSnList = getProductSnList();
		for(ProductBrief productBrief : productSnList){
			if(getSnOrImei(productBrief).equals(srcSn)){
				productBrief.setSnOrImei(srcSn);
				productBrief.setNewSnOrImei(destSn);
				break;
			}
		}
		saveProductSnList(productSnList);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ExchangeOrderViewProductAdapter(getActivity());
		mListView.setAdapter(mAdapter);
	
		mPayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					exchangeReplaceSN();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		ArrayList<ProductBrief> productSnList = getProductSnList();
		mAdapter.updateData(productSnList);
        mActionContainer.setVisibility(View.VISIBLE);
    	
        
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				ProductBrief productBrief = (ProductBrief) view.getTag();
				if(getNewSnOrImei(productBrief) == null){
					return false;
				}
                
				AlertDialog.Builder builder = new Builder(getActivity());
				
				final String sn = getSnOrImei(productBrief);
				builder.setMessage("删除的替换商品是:" + sn);
				builder.setTitle("删除替换商品");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						ArrayList<ProductBrief> productSnList = getProductSnList();
						for(ProductBrief productBrief : productSnList){
							if(getSnOrImei(productBrief).equals(sn)){
								productBrief.setNewSnOrImei(null);
								break;
							}
						}
						saveProductSnList(productSnList);
						mAdapter.updateData(productSnList);
						
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				builder.create().show();

				return true;
			}
		});

        
        getParent().getHomeButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
	}


	private void exchangeReplaceSN() throws JSONException {
		
		ArrayList<ProductBrief> productSnList = getProductSnList();
		final JSONArray snJsonArray = new JSONArray();
		for(ProductBrief productBrief : productSnList){
			if(getSnOrImei(productBrief) != null && getNewSnOrImei(productBrief) != null){
				JSONObject snJsonObject = new JSONObject();
				snJsonObject.put("srcSn", getSnOrImei(productBrief));
				snJsonObject.put("destSn", getNewSnOrImei(productBrief));
				snJsonArray.put(snJsonObject);
			}
		}
		
		getLoaderManager().restartLoader(EXCHANGE_REPLACE_LOADER, null, new LoaderCallbacks<ExchangeReplaceLoader.Result>() {

			@Override
			public ExchangeReplaceLoader onCreateLoader(int id, Bundle arg1) {
				if (id == EXCHANGE_REPLACE_LOADER) {
					
					ExchangeReplaceLoader exchangeReplaceLoader = new ExchangeReplaceLoader(getActivity(), snJsonArray);
					exchangeReplaceLoader.setNeedDatabase(false);
					exchangeReplaceLoader.setProgressNotifiable(mLoadingView);
					return exchangeReplaceLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(Loader<ExchangeReplaceLoader.Result> loader, ExchangeReplaceLoader.Result data) {
				if (data != null && data.responseInfo != null && data.responseInfo.equalsIgnoreCase("OK")) {
					ToastUtil.show(getActivity(), "当日换货成功");
					if (getActivity() != null) {
						getActivity().finish();
					}
				}else{
					ToastUtil.show(getActivity(), data.responseInfo);
					setSumbitButtonAttribute(true,R.string.return_resend);
				}

			}

			@Override
			public void onLoaderReset(Loader<ExchangeReplaceLoader.Result> arg0) {

			}
		});
	}
	
	public SameDayExchangeActivity getParent() {
		return (SameDayExchangeActivity) getActivity();
	}
	
	private void setSumbitButtonAttribute(boolean isClick, int resId) {
		mPayButton.setEnabled(isClick);
		mPayButton.setText(resId);
	}
	
	private void saveProductSnList(ArrayList<ProductBrief> productSnList) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(productSnList);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SharedPreferences mySharedPreferences = getActivity().getSharedPreferences(Constants.productSnCache, Activity.MODE_PRIVATE);
		// 将Product对象转换成byte数组，并将其进行base64编码
		String productBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		// 将编码后的字符串写到base64.xml文件中
		editor.putString("productSnList", productBase64);
		editor.commit();
	}
	
	private ArrayList<ProductBrief> getProductSnList(){
		ArrayList<ProductBrief> productSnObject = null;
		Map<String, ProductInfo> mProductInfoList = new HashMap<String, ProductInfo>();
		SharedPreferences pmySharedPreferences = getActivity().getSharedPreferences(Constants.productSnCache, Activity.MODE_PRIVATE);
		if(pmySharedPreferences != null){
			String productSnList = null;
			if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
				productSnList = pmySharedPreferences.getString("productSnList", null);
			}
			if(productSnList != null){
					String pproductBase64 = productSnList;
					if (pproductBase64 != null && pproductBase64.length() > 0) {
						// 对Base64格式的字符串进行解码
						byte[] base64Bytes = Base64.decode(pproductBase64.getBytes(), Base64.DEFAULT);
						ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
						ObjectInputStream ois = null;
						try {
							ois = new ObjectInputStream(bais);
						} catch (StreamCorruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 从ObjectInputStream中读取Product对象
						try {
							productSnObject = (ArrayList<ProductBrief>) ois.readObject();
						} catch (OptionalDataException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
			}
		}
		return productSnObject;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		ProductBrief productBrief = (ProductBrief) view.getTag();
		srcSn = getSnOrImei(productBrief);
		Intent intent = new Intent(getActivity(), ScannerActivity.class);
		intent.setAction(Constants.Intent.ACTION_SAMEDAYEXCHANGE_REPLACE_SN_SCAN);
		intent.putExtra(Constants.Intent.EXTRA_EXCHANGE_SRC_SN_STR, srcSn);
		startActivity(intent);
		SameDayExchangeActivity father = (SameDayExchangeActivity)getActivity();
		if(father.getExistCountInStack() == 1){
			
		}
		if (getActivity() != null) {
			getActivity().finish();
		}
	}
	
	
	
	
	public String getSnOrImei(ProductBrief productBrief){
		if(productBrief.mIsMobile.equals("0")){
			return productBrief.mSn;
		}else{
			return productBrief.mImei;
		}
	}
	
	public String getNewSnOrImei(ProductBrief productBrief){
		if(productBrief.mIsMobile.equals("0")){
			return productBrief.mNewSn;
		}else{
			return productBrief.mNewImei;
		}
	}

}
