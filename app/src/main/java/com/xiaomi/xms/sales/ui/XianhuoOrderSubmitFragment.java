package com.xiaomi.xms.sales.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.ShopIntentService.Listener;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.activity.XianhuoOrderActivity;
import com.xiaomi.xms.sales.adapter.XianhuoShoppingAdapter;
import com.xiaomi.xms.sales.loader.GetServiceNumberLoader;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.loader.XianhuoShoppingLoader;
import com.xiaomi.xms.sales.loader.XianhuoShoppingLoader.Result;
import com.xiaomi.xms.sales.loader.XianhuoWipeZeroLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.model.ProductInfo;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.TitleNode;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.xiaomi.xms.sales.zxing.ScannerActivity;

public class XianhuoOrderSubmitFragment extends BaseFragment {
	private final static int PRODUCT_DETAILS_LOADER = 100;
	private final static int WIPE_ZERO = 300;
    private final static int GET_SERVICENUMBER = 400;
	private EmptyLoadingView mLoadingView;
	private TextView mCount;
	private Button mSubmit;
	private String mJsonData;
	private XianhuoShoppingAdapter mAdapter;
	private BaseListView mListView;
	private ImageView mVcodeImage;
	private EditText mVcodeInput;
	private Button mChangeBtn;
	private boolean mNeedCheckCode;
	private BaseAlertDialog mCheckCodeDialog;
	private ShopIntentServiceAction mFetchDefenseVcodeAction;
	private ProgressDialog mProgressDialog;
	private View mListHeader;
	private View mListFooter;
	private View mBottom;
	private String mContainId;
	private String mMihomeId;
	private BigDecimal totalPrice;
	private BigDecimal newTotalPrice; // 抹零后的价格
	//private BigDecimal discountPirce; // 优惠金额
	private View mEmpty;
	private View mSNinputContainer;
	private EditText mSnInput;
	private Button mSnOkBtn;
	private Button mGoto;
	private ArrayList<Item> productItem;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.xianhuo_order_submit_fragment, container, false);
		mBottom = view.findViewById(R.id.bottom);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mListHeader = inflater.inflate(R.layout.use_coupon_header_view, null, false);
		mListFooter = inflater.inflate(R.layout.order_submit_footer, null, false);

		mListView.addHeaderView(mListHeader);
		mListView.addFooterView(mListFooter, null, false);
		mListView.setPadding(getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0, 
				getResources().getDimensionPixelSize(R.dimen.list_item_padding), 0);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mCount = (TextView) view.findViewById(R.id.count);
		mSubmit = (Button) view.findViewById(R.id.submit);
		totalPrice = new BigDecimal(0);
		newTotalPrice = new BigDecimal(0);
		//discountPirce = new BigDecimal(0);

		mEmpty = (View) view.findViewById(R.id.xianhuoempty);
		mEmpty.setVisibility(View.GONE);
		mGoto = (Button) view.findViewById(R.id.xianhuo_goto_button);
		
		mSNinputContainer = (View)view.findViewById(R.id.sn_input_container);
		mSNinputContainer.setVisibility(View.GONE);
		mSnInput = (EditText)view.findViewById(R.id.sn_input);
		mSnOkBtn = (Button)view.findViewById(R.id.sn_ok_button);
		
		mListFooter.setVisibility(View.GONE);
		productItem = new ArrayList<Item>();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBottom.setVisibility(View.GONE);
		mSubmit.setOnClickListener(mOnButtonClickListener);

		mGoto.setVisibility(View.GONE);
		mGoto.setOnClickListener(mOnButtonClickListener);

		mAdapter = new XianhuoShoppingAdapter(getActivity());
		mAdapter.hideArrow(true);
		mAdapter.showTopLine(true);
		mAdapter.showPaperBackground();
		mListView.setAdapter(mAdapter);

		mMihomeId = Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, "");

		Bundle bundle = getArguments();
		String sn = "";
		if (bundle != null) {
			boolean isError = bundle.getBoolean(Constants.Intent.EXTRA_ERROR_SN, false);
			if(isError){   //没扫到SN或是错误的就手动输入
				mSNinputContainer.setVisibility(View.VISIBLE);  //手动输入SN
				mListView.setVisibility(View.GONE);
				mBottom.setVisibility(View.GONE);
			}
			else{
				mContainId = bundle.getString(Constants.Intent.EXTRA_CONTAIN_ID);
				sn = bundle.getString(Constants.Intent.EXTRA_PRODUCT_ID);
				final String snStr = sn;
				//先生成订单号
				SharedPreferences numberCache = getActivity().getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE);
				if(numberCache == null || numberCache.getAll() == null || numberCache.getAll().isEmpty()){
					getLoaderManager().restartLoader(GET_SERVICENUMBER, null, new LoaderCallbacks<GetServiceNumberLoader.Result>(){
						@Override
						public Loader<GetServiceNumberLoader.Result> onCreateLoader(int id, Bundle arg1) {
							if (id == GET_SERVICENUMBER) {
								GetServiceNumberLoader mLoader = new GetServiceNumberLoader(getActivity());
								mLoader.setProgressNotifiable(mLoadingView);
								mLoader.setNeedDatabase(false);
								return (Loader<GetServiceNumberLoader.Result>) mLoader;
							}
							return null;
						}

						@Override
						public void onLoadFinished(Loader<GetServiceNumberLoader.Result> loader,GetServiceNumberLoader.Result data) {
							if(data != null && data.serviceNumber != null && data.serviceNumber.equalsIgnoreCase("") == false){
								Editor e = getActivity().getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE).edit();
								e.putString(Constants.serviceNumber, data.serviceNumber);
								e.commit();
								getProductInfo(snStr);  //获取商品信息
							}
							else{
								ToastUtil.show(getActivity(), "获取订单号失败！");
							}
						}

						@Override
						public void onLoaderReset(Loader<GetServiceNumberLoader.Result> arg0) {
						}
					});
				}
				else{
					getProductInfo(snStr);   //获取商品信息
				}
			}
			
			
		}

		mSnOkBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String snStr = mSnInput.getText().toString();
				if(snStr == null || snStr.length() == 0 || 
						(snStr.length() != Constants.SN_LENGTH && snStr.length() != Constants.NEW_SN_LENGTH 
						    && snStr.length() != Constants.DUOKAN_NEW_SN_LENGTH)){
					ToastUtil.show(getActivity(), "输入的SN错误！");
					return;
				}
				else{
					SharedPreferences numberCache = getActivity().getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE);
					if(numberCache == null || numberCache.getAll() == null || numberCache.getAll().isEmpty()){
						getLoaderManager().restartLoader(GET_SERVICENUMBER, null, new LoaderCallbacks<GetServiceNumberLoader.Result>(){
							@Override
							public Loader<GetServiceNumberLoader.Result> onCreateLoader(int id, Bundle arg1) {
								if (id == GET_SERVICENUMBER) {
									GetServiceNumberLoader mLoader = new GetServiceNumberLoader(getActivity());
									mLoader.setProgressNotifiable(mLoadingView);
									mLoader.setNeedDatabase(false);
									return (Loader<GetServiceNumberLoader.Result>) mLoader;
								}
								return null;
							}

							@Override
							public void onLoadFinished(Loader<GetServiceNumberLoader.Result> loader,GetServiceNumberLoader.Result data) {
								if(data != null && data.serviceNumber != null && data.serviceNumber.equalsIgnoreCase("") == false){
									Editor e = getActivity().getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE).edit();
									e.putString(Constants.serviceNumber, data.serviceNumber);
									e.commit();
									getProductInfo(snStr);  //获取商品信息
								}
								else{
									ToastUtil.show(getActivity(), "获取订单号失败！");
								}
							}

							@Override
							public void onLoaderReset(Loader<GetServiceNumberLoader.Result> arg0) {
							}
						});
					}
					else{
						getProductInfo(snStr);   //获取商品信息
					}
				}
			}
		});
		
		getParent().getHomeButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				//System.out.println("longclick:" + position);
				//LogUtil.i(getTag(), "onLongClick");

				final Item data = (Item) view.getTag();
				CartListNode node = null;
                if(data != null && data.getNode() != null){
                	node = (CartListNode) data.getNode();
                }
				AlertDialog.Builder builder = new Builder(getActivity());
				String snStr = "";
				if(node.getItemIds() != null && node.getItemIds().length() > 0){
					snStr = node.getItemIds();
				}
				final String sn = snStr;
				builder.setMessage("删除的商品是:" + sn);
				builder.setTitle("删除商品");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if(productItem != null && productItem.size() > 0){
							if (productItem.remove(data)) {
								//LogUtil.i(getTag(), "remove：true");
								ToastUtil.show(getActivity(), "删除商品：" + sn);
								// 删除持久化存储的商品信息
								SharedPreferences sp = getActivity().getSharedPreferences(Constants.productCache, Context.MODE_PRIVATE);
								if(sp != null && sp.getAll() != null && sp.getAll().size() > 0){
									Editor snEditor = sp.edit();
									snEditor.remove(sn);
									snEditor.commit();
								}

								updateView();  // 更新信息
							}
						}
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

	}

	/**
	 * 获得扫描的商品信息
	 */
	public void getProductInfo(final String sn) {
		getLoaderManager().restartLoader(PRODUCT_DETAILS_LOADER, null, new LoaderCallbacks<XianhuoShoppingLoader.Result>() {
			//获取sku
            String goodsId = sn != null ? sn.length() == Constants.SN_LENGTH ? sn.substring(0, Constants.SKU_LENGTH) :sn.substring(0, Constants.NEW_SKU_LENGTH) : "";
			@SuppressWarnings("unchecked")
			@Override
			public Loader<XianhuoShoppingLoader.Result> onCreateLoader(int id, Bundle bundle) {
				if (id == PRODUCT_DETAILS_LOADER && goodsId != null && goodsId.length() > 0) {
					mLoader = new XianhuoShoppingLoader(getActivity(), goodsId, mMihomeId, mContainId,sn);
					mLoader.setProgressNotifiable(mLoadingView);
					mLoader.setNeedDatabase(false);
					return (Loader<XianhuoShoppingLoader.Result>) mLoader;
				}
				return null;
			}

			@Override
			public void onLoadFinished(Loader<XianhuoShoppingLoader.Result> loader, XianhuoShoppingLoader.Result data) {
				boolean isFoundGoods = true;
				if (data == null || data.p == null || data.p.getProductPrice() == null 
						|| data.p.getProductPrice().length() == 0 || data.p.getProductPrice().equalsIgnoreCase("")) { 
					if(data != null && data.resultInfo != null && data.resultInfo.length() > 0){
						ToastUtil.show(getActivity(), data.resultInfo);
					}
					else{
						ToastUtil.show(getActivity(), "没查到商品或库存不足，请重新扫描商品SN！");
					}
					isFoundGoods = false;
				}
				else {
					isFoundGoods = true;
					if (data.p.isIsBatched()) {
						ToastUtil.show(getActivity(), "现货销售目前不支持套餐！");
					} 
					else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = null;
						try {
							oos = new ObjectOutputStream(baos);
							oos.writeObject(data.p);
						} catch (IOException e) {
							e.printStackTrace();
						}

						SharedPreferences mySharedPreferences = getActivity().getSharedPreferences(Constants.productCache, Activity.MODE_PRIVATE);
						// 将Product对象转换成byte数组，并将其进行base64编码
						String productBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
						SharedPreferences.Editor editor = mySharedPreferences.edit();
						// 将编码后的字符串写到base64.xml文件中
						editor.putString(sn, productBase64);
						editor.commit();
						
					}

				}
				if(isFoundGoods){
					mSNinputContainer.setVisibility(View.GONE); 
					mListView.setVisibility(View.VISIBLE);
					updateView(); // 更新页面
								  //加载打折促销信息
				}
				else{
					mSNinputContainer.setVisibility(View.VISIBLE);  //手动输入SN
					mListView.setVisibility(View.GONE);
					mBottom.setVisibility(View.GONE);
				}

			}

			@Override
			public void onLoaderReset(Loader<Result> arg0) {

			}
		});
	}

	/**
	 * 更新页面
	 */
	public void updateView() {
		
		Map<String, ProductInfo> mProductInfoList = new HashMap<String, ProductInfo>();
		
		SharedPreferences pmySharedPreferences = getActivity().getSharedPreferences(Constants.productCache, Activity.MODE_PRIVATE);
		if(pmySharedPreferences != null){
			Iterator<String> snMap = null;
			if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
				snMap = pmySharedPreferences.getAll().keySet().iterator();
			}
			if(snMap != null){
				while(snMap.hasNext()){
					String sn = snMap.next();
					String pproductBase64 = pmySharedPreferences.getString(sn, "");
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
							ProductInfo product = (ProductInfo) ois.readObject();
							mProductInfoList.put(sn, product);
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
		}

		if (mProductInfoList != null && mProductInfoList.size() > 0) {
			productItem = transToItem(mProductInfoList); // 商品对象的转换
			getLoaderManager().restartLoader(WIPE_ZERO, null, new LoaderCallbacks<XianhuoWipeZeroLoader.Result>() {
				BigDecimal oldTotalPrice = totalPrice;
               
				@Override
				public Loader<XianhuoWipeZeroLoader.Result> onCreateLoader(int id, Bundle arg1) {
					if (id == WIPE_ZERO && !TextUtils.isEmpty(mMihomeId)) {
						XianhuoWipeZeroLoader mLoader = new XianhuoWipeZeroLoader(getActivity(), mMihomeId, oldTotalPrice);
						mLoader.setProgressNotifiable(mLoadingView);
						mLoader.setNeedDatabase(false);
						return (Loader<XianhuoWipeZeroLoader.Result>) mLoader;
					}
					return null;
				}

				@Override
				public void onLoadFinished(Loader<XianhuoWipeZeroLoader.Result> arg0, XianhuoWipeZeroLoader.Result data) {
					if (data != null && data.newTotalPrice.doubleValue() > 0) {
						newTotalPrice = data.newTotalPrice;
					} else {
						newTotalPrice = oldTotalPrice;
					}
					updatePrice();
				}

				@Override
				public void onLoaderReset(Loader<XianhuoWipeZeroLoader.Result> arg0) {
				}

			});
		}
		mAdapter.updateData(productItem); // 更新信息

		mAdapter.updateTitleAndBlack();
		if (pmySharedPreferences == null || pmySharedPreferences.getAll() == null || pmySharedPreferences.getAll().isEmpty()) { // 没有商品
			mEmpty.setVisibility(View.VISIBLE);
			mGoto.setFocusable(false);
			mListView.setVisibility(View.GONE);
			mBottom.setVisibility(View.GONE);
		} else {
			mEmpty.setVisibility(View.GONE);
		}
	}

	public void updatePrice() {

		mListFooter.setVisibility(View.VISIBLE);
		mBottom.setVisibility(View.VISIBLE);

		mCount.setText(String.format(getString(R.string.order_submit_pay, newTotalPrice.doubleValue())));

		TextView productMoney = (TextView) mListFooter.findViewById(R.id.product_money);
		productMoney.setText(String.valueOf(totalPrice));
		/*
		 * TextView discountMoney = (TextView)
		 * mListFooter.findViewById(R.id.activity_discount);
		 * discountMoney.setText(String.valueOf(discountPirce));
		 * discountMoney.setVisibility(View.GONE); //暂时不显示
		 */
		TextView molingMoney = (TextView) mListFooter.findViewById(R.id.moling_money);
		molingMoney.setText(String.valueOf(newTotalPrice.subtract(totalPrice)));

		TextView amount = (TextView) mListFooter.findViewById(R.id.amount);
		amount.setText(String.valueOf(newTotalPrice));
		mAdapter.updateTitleAndBlack();
	}

	public ArrayList<Item> transToItem(Map<String, ProductInfo> productMap) {
		ArrayList<Item> items = new ArrayList<Item>();
		Item item = null;
		totalPrice = new BigDecimal(0);
		item = new Item();
		TitleNode tNode = new TitleNode();
		tNode.setTitle(ShopApp.getContext().getString(R.string.shopping_cartlist_title));
		item.setNode(tNode);
		item.setType(Item.TYPE_TITLE);
		items.add(item);

		Iterator<String> it = productMap.keySet().iterator();
		while (it.hasNext()) {
			String sn = it.next();
			ProductInfo p = productMap.get(sn);
			CartListNode node = new CartListNode();
			node.setCanChangeNum(true);
			node.setTitle(p.getProductName());
			node.setCount(1);
			node.setPrice(p.getProductPrice());
			totalPrice = totalPrice.add(BigDecimal.valueOf(Double.parseDouble(p.getProductPrice())));
			node.setTotal(p.getProductPrice());
			node.setItemId(p.getProductId());

			node.setPhoto(p.getImage());
			node.setThumbnail(p.getImage());
			// node.setBuyLimit()); //
			node.setCanDelete(true);
			node.setItemIds(sn); // 现在用于sn
			node.setShowType(""); //

			item = new Item();
			item.setType(Item.TYPE_CARTLIST);
			item.setNode(node);
			items.add(item);
		}

		item = new Item();
		item.setType(Item.TYPE_BLACK);
		items.add(item);

		return items;
	}

	public void setData(String jsonData) {
		mJsonData = jsonData;
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.title_ordersubmit);

	}

	public XianhuoOrderActivity getParent() {
		return (XianhuoOrderActivity) getActivity();
	}

	private void flushVerifyCode() {
		Activity activity = getActivity();
		Intent intent = new Intent(activity, ShopIntentService.class);
		intent.setAction(Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE);
		activity.startService(intent);
	}

	public void onFetchVcodeCompleted(String action, Intent intent) {
		ShopIntentService.unregisterAction(mFetchDefenseVcodeAction);
		if (Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE.equals(action)) {
			String url = intent.getStringExtra(Constants.Intent.EXTRA_CHECKCODE_URL);
			if (!TextUtils.isEmpty(url)) {
				ImageLoader.getInstance().loadImage(mVcodeImage, new Image(url), R.drawable.list_default_bg);
			} else {
				ToastUtil.show(getActivity(), R.string.fcode_vcode_fetch_err);
			}
		}
	}

	private OnClickListener mOnButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case BaseAlertDialog.BUTTON_NEGATIVE:
				break;
			case BaseAlertDialog.BUTTON_POSITIVE:
				generateOrder(v);
				break;
			case R.id.submit:
				if (mNeedCheckCode) {
					showCheckCodeDialog();
				} else {
					generateOrder(v);
				}
				break;
			case R.id.change:
				ShopIntentService.registerAction(mFetchDefenseVcodeAction);
				mVcodeInput.setText("");
				Utils.SoftInput.show(getActivity(), mVcodeInput);
				flushVerifyCode();
				break;
			case R.id.xianhuo_goto_button:
				Intent intent = new Intent(getActivity(), ScannerActivity.class);
				intent.setAction(Constants.Intent.ACTION_XIANHUO_SCAN);
				getActivity().startActivity(intent);
				if (getActivity() != null) {
					getActivity().finish();
				}
				break;
			default:
				break;
			}
		}
	};

	public void showCheckCodeDialog() {
		mFetchDefenseVcodeAction = new ShopIntentServiceAction(Constants.Intent.ACTION_FETCH_DEFENSE_HACKER_VCODE, (Listener) getActivity());
		ShopIntentService.registerAction(mFetchDefenseVcodeAction);
		flushVerifyCode();
		View checkCodeView = LayoutInflater.from(getActivity()).inflate(R.layout.check_code_item, null, false);
		mVcodeImage = (ImageView) checkCodeView.findViewById(R.id.vcode_image);
		mVcodeInput = (EditText) checkCodeView.findViewById(R.id.vcode);
		mChangeBtn = (Button) checkCodeView.findViewById(R.id.change);
		mChangeBtn.setOnClickListener(mOnButtonClickListener);
		mCheckCodeDialog = new BaseAlertDialog(getActivity());
		mCheckCodeDialog.setView(checkCodeView);
		mCheckCodeDialog.setTitle(R.string.checkcode_dialog_title);
		mCheckCodeDialog.setPositiveButton(R.string.dialog_ask_ok, mOnButtonClickListener, false);
		mCheckCodeDialog.setNegativeButton(R.string.dialog_ask_cancel, mOnButtonClickListener);
		mCheckCodeDialog.show();
	}

	private void generateOrder(View v) {
		SharedPreferences numberCache = getActivity().getSharedPreferences(Constants.serviceNumberCache, Context.MODE_PRIVATE);
		if(numberCache != null && numberCache.getAll() != null && numberCache.getAll().size() == 1){
			String serviceNumber = numberCache.getString(Constants.serviceNumber, "");
			LogUtil.i(getTag(), "处理的订单是："+serviceNumber);
			Intent intent = new Intent(getActivity(), ShopIntentService.class);
			intent.setAction(Constants.Intent.ACTION_XIANHUO_ORDER_SUBMIT);
			intent.putExtra(Constants.Intent.EXTRA_SHOP_INTENT_SERVICE_RETURN_JSON, mJsonData);
			intent.putExtra(Constants.Intent.EXTRA_ORDER_TYPE, Constants.xianhuo_orderType); // 发送现货销售标志
			intent.putExtra(Constants.Intent.EXTRA_REDUCE, 0);
			intent.putExtra(Constants.serviceNumber, serviceNumber);
			getActivity().startService(intent);
			setButtonState((Button) v, false, getString(R.string.order_submit_button_submit_n));
		}
		else{
			ToastUtil.show(getActivity(), "订单号还没有获取或订单信息有误！");
		}

	}

	public void onSubmitCallback(Intent intent) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

		if (!mNeedCheckCode) {
			setButtonState(mSubmit, true, getString(R.string.order_submit_button_submit));
		}
	}

	public void setButtonState(Button button, boolean isClick, String text) {
		button.setEnabled(isClick);
		button.setText(text);
	}

}
