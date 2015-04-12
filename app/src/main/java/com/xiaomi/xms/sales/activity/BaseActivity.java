package com.xiaomi.xms.sales.activity;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.UploadLogService;
import com.xiaomi.xms.sales.ShopIntentService.Listener;
import com.xiaomi.xms.sales.ShopIntentServiceAction;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.util.AppUpdater;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ThreadPool;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;
import com.xiaomi.xms.sales.widget.BaseLayout;
import com.xiaomi.xms.sales.widget.BaseLayout.BackKeyListener;
import com.xiaomi.xms.sales.widget.MenuPopupWindow;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;
import com.xiaomi.xms.sales.xmsf.account.LoginManager.AccountListener;
import com.xiaomi.xms.sales.xmsf.account.ui.LoginActivity;
import com.yeepay.bluetooth.pos.controll.BizConstant;
import com.yeepay.bluetooth.pos.controll.IController;
import com.yeepay.bluetooth.pos.controll.Me31Controller;
import com.yeepay.bluetooth.pos.user.BizResult;
import com.yeepay.bluetooth.pos.user.OnBluetoothConnectCallback;
import com.yeepay.bluetooth.pos.user.OnUserCallback;

public abstract class BaseActivity extends FragmentActivity implements Listener, AccountListener {
	private static final String TAG = "BaseActivity";
	private static final String HTTP_PREFIX = "http://";
	protected TextView mMenuItemOrders;
	protected TextView mMenuItemUpdate;
	protected TextView mMenuItemPrintAgain;
	protected TextView mMenuUploadLog;
	protected TextView mMenuItemURL;
	protected TextView mMenuItemSetting;
	protected TextView mMenuItemSwitch;
	protected TextView mMenuItemRefresh;
	private LinearLayout mMenuLayout;
	protected AppUpdater mUpdater;
	protected PopupWindow mMenuWindow;

	private ShopIntentServiceAction mCheckUpdateAction;
	private ShopIntentServiceAction mCheckActivityAction;
	private ShopIntentServiceAction mUpdateShoppingCountAction;

	private View mDecoratedView;
	private FrameLayout mContentContainer;
	private View mTitleBarContainer;
	protected View mShoppingStatusBar;
	private static int sShoppingCount = Constants.UNINITIALIZED_NUM;
	private static final long SHOPPING_COUNT_UPDATE_TIMEOUT = 60000L;
	private static long sLastTimeOfShoppingCountUpdate;
	private static int sOldShoppingCount = Constants.UNINITIALIZED_NUM;
	private TextView mTitle;
	protected View mHomeButton;
	private boolean mShoppingBarEnable = true;
	private boolean mHomeButtonEnable = true;
	private boolean mTitleBarEnable = true;
	protected boolean mUserCheckUpdate = false;
	protected Handler mHandler;
	
	private ProgressDialog mProgressDialog;
	private Set<OnRefreshListener> mRefreshListenerSet;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (mMenuWindow != null) {
			if (ShopApp.DEBUG) {
				mMenuItemSwitch.setVisibility(View.VISIBLE);
				if (Utils.Preference.getBooleanPref(this, ShopApp.PREF_USER_DEBUG, true)) {
					mMenuItemSwitch.setText(R.string.menu_switch_formal);
				} else {
					mMenuItemSwitch.setText(R.string.menu_switch_test);
				}
				mMenuItemURL.setVisibility(View.VISIBLE);
			} else {
				mMenuItemSwitch.setVisibility(View.GONE);
				mMenuItemURL.setVisibility(View.GONE);
			}
			
			mMenuItemUpdate.setVisibility(View.GONE);
			if (!mMenuWindow.isShowing()) {
				mMenuWindow.showAtLocation(findViewById(R.id.popup_parent), Gravity.BOTTOM, 0, 0);
			}
		}
		return false;// 返回为true 则显示系统menu
	}

	public void setShoppingBarEnable(boolean enable) {
		mShoppingBarEnable = enable;
		mShoppingStatusBar.setVisibility(mShoppingBarEnable ? View.VISIBLE : View.GONE);
	}

	public void setHomeButtonEnable(boolean enable) {
		mHomeButtonEnable = enable;
		mHomeButton.setVisibility(mHomeButtonEnable ? View.VISIBLE : View.GONE);
	}

	public View getHomeButton() {
		return mHomeButton;
	}

	public void setTitleBarEnable(boolean enable) {
		mTitleBarEnable = enable;
		mTitleBarContainer.setVisibility(mTitleBarEnable ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setTitle(int resId) {
		if (mTitle != null) {
			mTitle.setText(resId);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		if (mTitle != null) {
			mTitle.setText(title);
		}
	}

	public void setLeftView(View view) {
		LinearLayout l = (LinearLayout) findViewById(R.id.custom_container);
		l.setGravity(Gravity.CENTER);
		l.removeAllViewsInLayout();
		l.addView(view);
	}

	public void setRightView(View view) {
		LinearLayout l = (LinearLayout) findViewById(R.id.title_right_bar);
		l.removeAllViewsInLayout();
		l.addView(view);
	}

	/**
	 * 刷新界面
	 */
	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void registerRefreshListener(OnRefreshListener l) {
		if (mRefreshListenerSet == null) {
			mRefreshListenerSet = new HashSet<OnRefreshListener>();
		}
		mRefreshListenerSet.add(l);
	}

	public void unregisterRefreshListener(OnRefreshListener l) {
		if (mRefreshListenerSet != null) {
			mRefreshListenerSet.remove(l);
		}
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		ShopApp.initCookiesIfHaveNot();

		mUpdater = new AppUpdater(this);
		mHandler = new Handler();
		BaseLayout baseLayout = (BaseLayout) LayoutInflater.from(this).inflate(R.layout.base_activity, null);
		setContentView(baseLayout);
		baseLayout.setActivity(this);
		BackKeyListener backKeyListener = getBackKeyListener();
		if (backKeyListener != null) {
			baseLayout.setBackKeyListener(backKeyListener);
		}
		mContentContainer = (FrameLayout) findViewById(R.id.content_container);
		mTitleBarContainer = findViewById(R.id.title_bar_container);
		mShoppingStatusBar = findViewById(R.id.title_bar_custom_view);
		mShoppingStatusBar.setOnClickListener(mClickListener);
//		mShoppingStatusBar.setVisibility(View.GONE);
		mTitle = (TextView) findViewById(R.id.title_bar_title);
		mTitle.setText( getString(R.string.app_name));
		mHomeButton = findViewById(R.id.title_bar_home);
		mHomeButton.setOnClickListener(mClickListener);

		LoginManager.getInstance().addLoginListener(this);

		mMenuLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.menu_list, null);
		mMenuItemSetting = (TextView) mMenuLayout.findViewById(R.id.menu_setting);
		mMenuItemSetting.setOnClickListener(mMenuItemClick);
		mMenuItemOrders = (TextView) mMenuLayout.findViewById(R.id.menu_orders);
		mMenuItemOrders.setOnClickListener(mMenuItemClick);
		mMenuItemRefresh = (TextView) mMenuLayout.findViewById(R.id.menu_refresh);
		mMenuItemRefresh.setOnClickListener(mMenuItemClick);
		mMenuItemSwitch = (TextView) mMenuLayout.findViewById(R.id.menu_switch);
		mMenuItemSwitch.setOnClickListener(mMenuItemClick);
		mMenuItemUpdate = (TextView) mMenuLayout.findViewById(R.id.menu_update);
		mMenuItemUpdate.setOnClickListener(mMenuItemClick);
		mMenuItemPrintAgain = (TextView) mMenuLayout.findViewById(R.id.menu_printagain);
		mMenuItemPrintAgain.setOnClickListener(mMenuItemClick);
		mMenuUploadLog = (TextView) mMenuLayout.findViewById(R.id.menu_uploadlog);
		mMenuUploadLog.setOnClickListener(mMenuItemClick);
		mMenuItemURL = (TextView) mMenuLayout.findViewById(R.id.menu_url);
		mMenuItemURL.setOnClickListener(mMenuItemClick);

		mMenuWindow = new MenuPopupWindow(BaseActivity.this, mMenuLayout);
		
		
		mProgressDialog = new ProgressDialog(BaseActivity.this);
        mProgressDialog.setCancelable(false);
        setShoppingBarEnable(false);
	}

	protected BackKeyListener getBackKeyListener() {
		return null;
	}

	protected Fragment newFragmentByTag(String tag) {
		return null;
	}

	public void showFragment(String tag, Bundle bundle, boolean addToBackStack) {
		if (mDecoratedView == null) {
			LogUtil.w(TAG, "mDecoratedView is NOT FOUND.");
			return;
		}
		if (mDecoratedView.getId() <= 0) {
			throw new IllegalArgumentException("The activity in xml layout MUST has argument 'id'.");
		}

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		Fragment fragment = getFragmentByTag(tag);
		if (fragment == null) {
			fragment = newFragmentByTag(tag);
			if (bundle != null) {
				fragment.setArguments(bundle);
			}
		}

		if (fragment == null) {
			LogUtil.w(TAG, "NO fragment found by tag: " + tag);
			return;
		}

		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(mDecoratedView.getId(), fragment, tag);
		if (addToBackStack) {
			ft.addToBackStack(null);
		}
		ft.commitAllowingStateLoss();
	}

	public Fragment getFragmentByTag(String tag) {
		FragmentManager fm = getSupportFragmentManager();
		return fm.findFragmentByTag(tag);
	}

	protected void setCustomContentView(int layoutResId) {
		View decorateView = View.inflate(this, layoutResId, null);
		if (decorateView != null) {
			if (mDecoratedView != null) {
				mContentContainer.removeView(mDecoratedView);
			}
			mContentContainer.addView(decorateView);
			mDecoratedView = decorateView;
		} else if (mDecoratedView != null) {
			mContentContainer.removeView(mDecoratedView);
			mDecoratedView = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LoginManager.getInstance().removeLoginListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mShoppingBarEnable && LoginManager.getInstance().hasLogin()) {
			if (sShoppingCount == Constants.UNINITIALIZED_NUM || (System.currentTimeMillis() - sLastTimeOfShoppingCountUpdate) > SHOPPING_COUNT_UPDATE_TIMEOUT) {// 未初始化过或长期未更新（考虑与主站的同步），发起更新请求
				updateShoppingCount();
			} else {// 直接更新view
//				updateShoppingCountView();
			}
		}
	}

	public void updateShoppingCount() {
		if (mUpdateShoppingCountAction == null) {
			mUpdateShoppingCountAction = new ShopIntentServiceAction(Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT, this);
		}
		ShopIntentService.registerAction(mUpdateShoppingCountAction);
		Intent intent = new Intent(this, ShopIntentService.class);
		intent.setAction(Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT);
		startService(intent);
	}

	/**
	 * 检查服务端是否有活动
	 */
	public void checkActivity() {
		if (mCheckActivityAction == null) {
			mCheckActivityAction = new ShopIntentServiceAction(Constants.Intent.ACTION_CHECK_ACTIVITY, this);
		}
		ShopIntentService.registerAction(mCheckActivityAction);
		Intent intent = new Intent(this, ShopIntentService.class);
		intent.setAction(Constants.Intent.ACTION_CHECK_ACTIVITY);
		startService(intent);
	}

	public void startCampaignActivityWithAnimation(String url) {
		Intent intent = new Intent(this, CampaignActivity.class);
		intent.putExtra(Constants.Intent.EXTRA_COMPAIGN_URL, url);
		intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_BOTTOM, CampaignActivity.BOTTOM_STYLE_BUTTON);
		intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_SHOW_TITLE, false);
		intent.putExtra(Constants.Intent.EXTRA_CAMPAIGN_FINISH_ANIM, true);
		startActivity(intent);
		overridePendingTransition(R.anim.campaign_appear_enter, R.anim.campaign_appear_exit);
	}

	private void onCheckActivityFinished(Intent callbackIntent) {
		String url = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_URL);
		String version = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_VERSION);
		String type = callbackIntent.getStringExtra(Constants.Intent.EXTRA_ACTIVITY_TYPE);
		boolean hasActivity = !TextUtils.isEmpty(url) && !TextUtils.isEmpty(version);
		if (hasActivity) {
			String prevVersion = Utils.Preference.getStringPref(this, Constants.Prefence.PREF_ACTIVITY_VERSION, null);
			Utils.Preference.setStringPref(this, Constants.Prefence.PREF_ACTIVITY_VERSION, version);
			// 如果是同一个活动并且类型是预约，那么只弹出一次
			if (TextUtils.equals(prevVersion, version) && TextUtils.equals(type, Tags.Activity.TYPE_RESERVE)) {
				return;
			}

			// 如果之前的活动和现在活动页不一致，那么重新加载活动页
			Utils.Preference.setStringPref(this, Constants.Prefence.PREF_ACTIVITY_URL, url);
			startCampaignActivityWithAnimation(url);
		}
	}

	@Override
	public void onServiceCompleted(String action, Intent callbackIntent) {
		if (Constants.Intent.ACTION_UPDATE_SHOPPING_COUNT.equals(action)) {
			ShopIntentService.unregisterAction(mUpdateShoppingCountAction);
			sShoppingCount = callbackIntent.getIntExtra(Constants.Intent.EXTRA_SHOPPING_COUNT, Constants.UNINITIALIZED_NUM);
			if (sShoppingCount != Constants.UNINITIALIZED_NUM) {
				sLastTimeOfShoppingCountUpdate = System.currentTimeMillis();
			}
			if (sOldShoppingCount != sShoppingCount) {
				sOldShoppingCount = sShoppingCount;
			}
		} else if (Constants.Intent.ACTION_CHECK_ACTIVITY.equals(action)) {
			ShopIntentService.unregisterAction(mCheckActivityAction);
			onCheckActivityFinished(callbackIntent);
		} else if (TextUtils.equals(action, Constants.Intent.ACTION_CHECK_UPDATE)) {
			ShopIntentService.unregisterAction(mCheckUpdateAction);
			String updateUrl = callbackIntent.getStringExtra(Constants.Intent.EXTRA_UPDATE_URL);
			String versionName = callbackIntent.getStringExtra(Constants.Intent.EXTRA_UPDATE_VERSION_NAME);
			String updateSummary = callbackIntent.getStringExtra(Constants.Intent.EXTRA_UPDATE_SUMMARY);
			LogUtil.d(TAG, "url:" + updateUrl);
			if (!TextUtils.isEmpty(updateUrl) && !TextUtils.isEmpty(versionName)) {
				mUpdater.loadVersionLogAndPopDialog(versionName, updateUrl, updateSummary);
			} else {
				Utils.Preference.setLongPref(this, Constants.AppUpdate.PREF_LAST_UPDATE_IS_OK, System.currentTimeMillis());
				if (mUserCheckUpdate) {
					ToastUtil.show(this, R.string.no_update);
				}
			}
			mUserCheckUpdate = false;
		}
	}



	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.title_bar_custom_view:
				System.exit(-1);
//				Intent intent = new Intent(BaseActivity.this, ICUMainActivity.class);
//				startActivity(intent);
//				finish();
//				if (LoginManager.getInstance().hasLogin()) {
//					gotoShoppingCart();
//				} else {
//					ToastUtil.show(BaseActivity.this, R.string.login_before_check_shopping_cart);
//					gotoAccount();
//				}
				break;
			case R.id.title_bar_home:
				onBackPressed();
				break;
			default:
				break;
			}
		}
	};

	private void popLoadURLMenu() {
		final BaseAlertDialog dialog = new BaseAlertDialog(this);
		final EditText edit = new EditText(this);
		String url = Utils.Preference.getStringPref(this, Constants.Prefence.PREF_ACTIVITY_URL, "");
		edit.setText(url);
		dialog.setView(edit);
		dialog.setPositiveButton(R.string.cancel, null);
		dialog.setNegativeButton(R.string.dialog_ask_ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = edit.getText().toString();
				if (TextUtils.isEmpty(url)) {
					return;
				}
				if (TextUtils.indexOf(url, HTTP_PREFIX) != 0) {
					url = HTTP_PREFIX + url;
				}
				startCampaignActivityWithAnimation(url);
			}
		});
		dialog.show();
	}

	private OnClickListener mMenuItemClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.menu_url:
				mMenuWindow.dismiss();
				popLoadURLMenu();
				break;
			case R.id.menu_update:
				mMenuWindow.dismiss();
				mUserCheckUpdate = true;
				checkUpdate(true);
				ToastUtil.show(ShopApp.getContext(), R.string.checking_update);
				break;
			case R.id.menu_setting:
				mMenuWindow.dismiss();
				showSetting();
				break;
			case R.id.menu_switch:
				mMenuWindow.dismiss();
				if (TextUtils.equals(mMenuItemSwitch.getText(), getString(R.string.menu_switch_formal))) {
					mMenuItemSwitch.setText(R.string.menu_switch_formal);
					ShopApp.setUserPrefDebug(false);
				} else {
					mMenuItemSwitch.setText(R.string.menu_switch_test);
					ShopApp.setUserPrefDebug(true);
				}
				ToastUtil.show(BaseActivity.this, R.string.switch_formal_test_hint);
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				}, 2000);
				break;
			case R.id.menu_refresh:
				mMenuWindow.dismiss();
				if (mRefreshListenerSet != null) {
					for (OnRefreshListener l : mRefreshListenerSet) {
						l.onRefresh();
					}
				}
				break;
			case R.id.menu_printagain:
				mMenuWindow.dismiss();
				boolean isConnected = false;
				final IController mController = Me31Controller.getInstance();
				mController.setUserCallback(new OnUserCallback() {

			    	 @Override
			         public void onTestData(final int bizType, final String data) {
			             LogUtil.i(TAG, "onTestData data = " + data);
			         }

			         @Override
			         public void onSuccess(int bizType, BizResult result) {
			             LogUtil.i(TAG, "onSuccess BizResult = " + result);
			             /**
			              * 这个接口如果是消费类型的话回调不一定代表成功消费 只是代表期间没有异常，需要判断mSuccess是否为TRUE
			              */
			
			         }

			         @Override
			         public void onStart(int bizType) {
			             switch (bizType) {
			                 case BizConstant.BIZ_SIGNIN:
			                     mProgressDialog.setMessage("正在签到, 请稍等...");
			                     break;
			                 case BizConstant.BIZ_CONSUME:
			                     mProgressDialog.setMessage("开始支付, 请稍等...");
			                     break;
			                 case BizConstant.BIZ_CANCEL_CONSUME:
			                     mProgressDialog.setMessage("正在撤销消费, 请稍等...");
			                     break;
			                 default:
			                     break;
			             }
			             if(!isFinishing()){
			            	 mProgressDialog.show();
			             }
			             
			         }

			         @Override
			         public void onPrintStart(final int bizType) {
			         }

			         @Override
			         public void onPrintEnd(int bizType) {
			        	 mController.disConnect(BaseActivity.this);
			         }

			         @Override
			         public void onError(int bizType, int errorType) {
			        	 LogUtil.i(TAG, "onError BizResult = " + errorType);
			        	 ToastUtil.show(BaseActivity.this, R.string.printer_error);
			        	 switch (bizType) {
			        	 case BizConstant.BIZ_PRINT:
			              	if(errorType == BizConstant.BIZ_ERROR_NO_PAPER ){
			              		ToastUtil.show(BaseActivity.this, "Pos机无纸，请加纸再重新打印");
			              	}
			              	break;
			        	 }
			         
			             
			         }

			         @Override
			         public void onEnd(int bizType) {
			         }

			         @Override
			         public void onConnectLost(Throwable arg0) {
			             LogUtil.i(TAG, "onConnectLost");

			           
			         }

			         @Override
			         public void onInputPin() {
			         	mProgressDialog.setMessage("请输入银行卡密码...");
			             
			         }

			         @Override
			         public void onSwipeCard() {
			         	mProgressDialog.setMessage("请刷卡...");
			             
			         }

			 		@Override
			 		public void onInputPinEnd() {
			 			mProgressDialog.setMessage("验证中...");
			 		}

			 		@Override
			 		public void onSendData(int arg0) {
			 			//mProgressDialog.setMessage("数据发送中...");
			 		}

			 		@Override
			 		public void onSwipeCardEnd(String arg0) {
			 			mProgressDialog.setMessage("刷卡成功");
			 			
			 		}
			    	
			    });
				try{
					isConnected = mController.isConnected();
				}catch(Exception e){
					String address = Utils.Preference.getStringPref(BaseActivity.this, Constants.Account.PREF_POS_MAC_ADDRESS, "");
					mController.connect(BaseActivity.this, address, new OnBluetoothConnectCallback() {

				        @Override
				        public void onConnectStar() {
				            LogUtil.i(TAG, "onConnectStar");
				            mProgressDialog.setMessage("正在连接蓝牙设备...");
				            mProgressDialog.show();
				        }

				        @Override
				        public void onConnectLost(Throwable arg0) {
				            LogUtil.i(TAG, "onConnectLost");
				            mProgressDialog.dismiss();
				        }

				        @Override
				        public void onConnectFailed() {
				            LogUtil.i(TAG, "onConnectFailed");
				            ToastUtil.show(BaseActivity.this, R.string.bluetooth_connect_error_info);
				        }

				        @Override
				        public void onConnectEnd() {
				            LogUtil.i(TAG, "onConnectEnd");
				          
				            mProgressDialog.dismiss();
				        }

				        @Override
				        public void onConnectSuccess() {
				            LogUtil.i(TAG, "onConnectSuccess");
				            mController.setServerIp(HostManager.YeePay.SERVERIP, HostManager.YeePay.SERVERPORT);
								try{
									mController.printPre();
									ToastUtil.show(BaseActivity.this, R.string.bluetooth_pos_reprint);
								}catch(Exception e){
									ToastUtil.show(BaseActivity.this, R.string.printer_error);
								}
							
				        }
				    });
				}
				if (isConnected) {
					try{
						mController.printPre();
					}catch(Exception e){
						ToastUtil.show(BaseActivity.this, R.string.printer_error);
						return;
					}
				} else {
					ToastUtil.show(BaseActivity.this, R.string.print_again_error);
				}
				break;
			case R.id.menu_uploadlog:
				mMenuWindow.dismiss();
				Intent intent = new Intent(BaseActivity.this, UploadLogService.class);
                startService(intent);
				break;

			default:
				break;
			}
		}
	};

	public void gotoAccount() {
		LoginManager loginManager = LoginManager.getInstance();
		if (loginManager.hasSystemAccount() && Utils.Preference.getBooleanPref(this, Constants.Prefence.PREF_MIUI_ACCOUNT_AVAILABLE, false)) {
			showSystemLoginDialog();
		} else {
			gotoLogin();
		}
	}

	protected void gotoShoppingCart() {
		Intent intent = new Intent(this, ShoppingActivity.class);
		startActivity(intent);
	}

	protected void gotoMiHomeShoppingCart(String mihomeId) {
		Intent intent = new Intent(this, ShoppingActivity.class);
		intent.putExtra(Constants.Intent.EXTRA_MIHOME_BUY, mihomeId);
		startActivity(intent);
	}

	private void showSetting() {
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}

	@Override
	public void onLogin(String userId, String authToken, String security) {
		updateShoppingCount();
	}

	@Override
	public void onLogout() {
		updateShoppingCount();
	}

	@Override
	public void onInvalidAuthonToken() {
	}

	public void checkUpdate(boolean force) {
		if (mCheckUpdateAction == null) {
			mCheckUpdateAction = new ShopIntentServiceAction(Constants.Intent.ACTION_CHECK_UPDATE, this);
		}
		ShopIntentService.registerAction(mCheckUpdateAction);
		mUpdater.sendCheckApkUpdateService(force);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.RequestCode.CODE_REQUEST_SIGIN:
			if (resultCode == Activity.RESULT_OK) {
				String userId = data.getStringExtra(LoginActivity.KEY_USER_ID);
				String serviceToken = data.getStringExtra(LoginActivity.KEY_SERVICE_TOKEN);
				String security = data.getStringExtra(LoginActivity.KEY_SECURITY);
				LoginManager.getInstance().onAccountLoginSucceed(userId, serviceToken, security);
			}
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	private void showSystemLoginDialog() {
		BaseAlertDialog dialog = new BaseAlertDialog(this);
		dialog.setTitle(R.string.autologin_title);
		dialog.setMessage(getResources().getString(R.string.autologin_summary, LoginManager.getInstance().getSystemAccountId()));
		dialog.setPositiveButton(R.string.autologin_ask_ok, new OnClickListener() {
			@Override
			public void onClick(View v) {
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						final String authToken = LoginManager.getInstance().getSystemAccountAuthToken(Constants.Account.DEFAULT_SERVICE_ID);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(authToken)) {
									LoginManager.getInstance().loginSystem(authToken);
								} else {
									ToastUtil.show(BaseActivity.this, R.string.login_system_failed);
								}
							}
						});
					}
				});
			}
		});
		dialog.setNegativeButton(R.string.autologin_ask_cancel, new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginManager.getInstance().setSystemLogin(false);
				gotoLogin();
			}
		});
		dialog.show();
	}

	public void gotoLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(Constants.Account.EXTRA_SERVICE_URL, Constants.Account.DEFAULT_SERVICE_ID);
		startActivityForResult(intent, Constants.RequestCode.CODE_REQUEST_SIGIN);
	}

	public View getTitleBarContainer() {
		return mTitleBarContainer;
	}

	
	
	
	    
}
