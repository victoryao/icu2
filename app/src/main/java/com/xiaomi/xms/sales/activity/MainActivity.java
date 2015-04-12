package com.xiaomi.xms.sales.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.UploadLogService;
import com.xiaomi.xms.sales.adapter.FragmentPagerAdapter;
import com.xiaomi.xms.sales.adapter.FragmentPagerAdapter.TabChangedListener;
import com.xiaomi.xms.sales.db.DBContract.DataMimeType;
import com.xiaomi.xms.sales.db.DBContract.DataStats;
import com.xiaomi.xms.sales.db.RegionDBHelper;
import com.xiaomi.xms.sales.db.RegionDBHelper.RegionInfo;
import com.xiaomi.xms.sales.ui.AttentionTestFragment;
import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.ui.CategoryFragment;
import com.xiaomi.xms.sales.ui.RASSTestFragment;
import com.xiaomi.xms.sales.ui.ThinkingTestFragment;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseLayout.BackKeyListener;
import com.xiaomi.xms.sales.widget.TabIndicator;

public class MainActivity extends BaseActivity implements TabChangedListener,
		BackKeyListener {
	private static final String TAG = "MainActivity";

	private ViewPager mViewPager;
	private FragmentPagerAdapter mPagerAdapter;

	private BaseFragment mHomeFragment;
	private BaseFragment mCategoryFragment;
	private BaseFragment mLotteryFragment;

	public static final String FRAGMENT_TAG_HOME = "fragment_tag_home";
	public static final String FRAGMENT_TAG_CATEGORY = "fragment_tag_category";
	public static final String FRAGMENT_TAG_LOTTERY = "fragment_tag_lottery";

	private static final String STATE_TAB_LEFT_MARGIN = "tabLeftMargin";
	private static final String STATE_TAB_RIGHT_MARGIN = "tabRightMargin";

	private TabIndicator mTabHome;
	private TabIndicator mTabCategory;
	private TabIndicator mTabLottery;
	public View tabs;
	private View mTabContainerBg;
	private String mCurrentTag = FRAGMENT_TAG_HOME;

	private int mBackKeyPressedCount;
	private SensorManager sensorMag;
	private Sensor gravitySensor;
	private boolean mScreenStatus;
	private static final int EXIT_DURATION_BETWEEN_BACKKEY_PRESSED = 2 * 1000;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setCustomContentView(R.layout.main_activity);
		tabs = findViewById(R.id.tabs);
		mTabContainerBg = findViewById(R.id.tab_container_bg);
		mViewPager = (ViewPager) findViewById(R.id.tab_pager);
		mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());
		mPagerAdapter.setTabChangedListener(this);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener());
		
		// 立刻检查活动信息
		// checkActivity();
		// 初始化传感器
		initGravitySensor();
		setUpTabAndFragments();
		configTabAndFragments();
		setHomeButtonEnable(false);
		setShoppingBarEnable(false);
		// 新年祝福, 每年的新年都可能用到，不要删掉这段代码
		// initFestivalAlarm();
//		delayedCheckedWorks();

	}

	/**
	 * 初始化传感器
	 */
	private void initGravitySensor() {
		sensorMag = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		gravitySensor = sensorMag.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	private void updateRegionDB() {
		String version = null;
		Cursor c = this.getContentResolver().query(DataStats.CONTENT_URI,
				new String[] { DataStats.STATS },
				DataStats.TYPE + "='" + DataMimeType.REGION + "'", null, null);
		try {
			if (c != null && c.getCount() > 0) {
				if (c.moveToFirst()) {
					version = c.getString(0);
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		RegionInfo region = RegionDBHelper.getInstance(MainActivity.this)
				.readFromResource();
		if (!TextUtils.equals(version,
				region.configs.get(RegionDBHelper.HEADER.VERSION))) {
			RegionDBHelper.getInstance(MainActivity.this)
					.updateRegionDB(region);
		}

	}

	private void delayedCheckedWorks() {
		// 可以在启动后延迟一段时间再做的工作，目的是为了使主要的工作得到优先执行
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// 检查系统更新
				checkUpdate(true);
				// 更新region
				updateRegionDB();
			}
		}, 3 * 1000);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);
		mMenuItemUpdate.setVisibility(View.VISIBLE);
		return false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		configTabAndFragments();
	}

	private void configTabAndFragments() {

		int position = mPagerAdapter.getPositionByTag(mCurrentTag);
		mPagerAdapter.selectTab(position);
		mViewPager.setCurrentItem(position);
	}

	private void setUpTabAndFragments() {
		// Init the tabindicators
		mTabHome = (TabIndicator) findViewById(R.id.tab_indicator_home);
		mTabCategory = (TabIndicator) findViewById(R.id.tab_indicator_category);
		mTabLottery = (TabIndicator) findViewById(R.id.tab_indicator_lottery);

		// Setup fragments
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mHomeFragment = (BaseFragment) manager
				.findFragmentByTag(FRAGMENT_TAG_HOME);
		if (mHomeFragment == null) {
			mHomeFragment = new RASSTestFragment();
			transaction.add(R.id.tab_pager, mHomeFragment, FRAGMENT_TAG_HOME);
		}
		mCategoryFragment = (BaseFragment) manager
				.findFragmentByTag(FRAGMENT_TAG_CATEGORY);
		if (mCategoryFragment == null) {
			mCategoryFragment = new AttentionTestFragment();
			transaction.add(R.id.tab_pager, mCategoryFragment,
					FRAGMENT_TAG_CATEGORY);
		}
		mLotteryFragment = (BaseFragment) manager
				.findFragmentByTag(FRAGMENT_TAG_LOTTERY);
		if (mLotteryFragment == null) {
			mLotteryFragment = new ThinkingTestFragment();
			transaction.add(R.id.tab_pager, mLotteryFragment,
					FRAGMENT_TAG_LOTTERY);
		}

		mPagerAdapter.addFragment(mTabHome, mHomeFragment);
		mPagerAdapter.notifyDataSetChanged();
		mPagerAdapter.addFragment(mTabCategory, mCategoryFragment);
		mPagerAdapter.notifyDataSetChanged();
		mPagerAdapter.addFragment(mTabLottery, mLotteryFragment);
		mPagerAdapter.notifyDataSetChanged();

		if (!transaction.isEmpty()) {
			transaction.commitAllowingStateLoss();
			getSupportFragmentManager().executePendingTransactions();
		}
	}

	@Override
	public void onTabChanged(int position) {
		mViewPager.setCurrentItem(position);
	}

	private class OnPageChangeListener extends SimpleOnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTabContainerBg
					.getLayoutParams();
			float leftMargin = getResources().getDimensionPixelSize(
					R.dimen.tab_container_bg_leftmargin);
			float rightMargin = 0f;
			TabIndicator tabIndicator = mPagerAdapter.getTab(position);
			if (tabIndicator != null) {
				float offset = positionOffset * tabIndicator.getWidth();
				leftMargin += offset;
				rightMargin -= offset;
				for (int i = 1; i <= position; i++) {
					leftMargin += tabIndicator.getWidth();
					rightMargin -= tabIndicator.getWidth();
				}
				layoutParams.leftMargin = (int) leftMargin;
				layoutParams.rightMargin = (int) rightMargin;
				mTabContainerBg.setLayoutParams(layoutParams);
			} else {
				mViewPager.setCurrentItem(position);
			}
		}

		@Override
		public void onPageSelected(int position) {
			// 更新Tab的选中状态
			mPagerAdapter.selectTab(position);
			mCurrentTag = mPagerAdapter.getTagByPosition(position);
			if (!mCurrentTag.equals(FRAGMENT_TAG_HOME)) {
				if (mHomeFragment != null) {
					// ((HomeFragment) mHomeFragment).switcherImage();
				}
			}
			configScreenOrientation();
		}
	}

	private void configScreenOrientation() {
		if (TextUtils.equals(mCurrentTag, FRAGMENT_TAG_HOME)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.RequestCode.CODE_REQUEST_HOME_FULL_SCREEN:
			if (resultCode == Activity.RESULT_CANCELED) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		// 删除所有的登录信息
		Utils.Preference.removePref(this, Constants.Account.PREF_UID);
		Utils.Preference
				.removePref(this, Constants.Account.PREF_EXTENDED_TOKEN);
		Utils.Preference.removePref(this, Constants.Account.PREF_PASS_TOKEN);
		Utils.Preference.removePref(this, Constants.Account.PREF_SYSTEM_UID);
		Utils.Preference.removePref(this,
				Constants.Account.PREF_SYSTEM_EXTENDED_TOKEN);
		Utils.Preference.removePref(this, Constants.Account.PREF_LOGIN_SYSTEM);
		Utils.Preference.removePref(this, Constants.Account.PREF_USER_ORGID);
		Utils.Preference.removePref(this, Constants.Account.PREF_USER_NAME);
		Utils.Preference.removePref(this, Constants.Account.PREF_USER_ORGNAME);
		super.onDestroy();
	}

	public static void launchMain(Context context, String fragmentTag) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constants.Intent.EXTRA_GO_TO_FRAGMENT, fragmentTag);
		context.startActivity(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		RelativeLayout.LayoutParams layoutParams = (LayoutParams) mTabContainerBg
				.getLayoutParams();
		outState.putInt(STATE_TAB_LEFT_MARGIN, layoutParams.leftMargin);
		outState.putInt(STATE_TAB_RIGHT_MARGIN, layoutParams.rightMargin);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		RelativeLayout.LayoutParams layoutParams = (LayoutParams) mTabContainerBg
				.getLayoutParams();
		layoutParams.leftMargin = savedInstanceState
				.getInt(STATE_TAB_LEFT_MARGIN);
		layoutParams.rightMargin = savedInstanceState
				.getInt(STATE_TAB_RIGHT_MARGIN);
		LogUtil.d(TAG, "Restore tab container margin left:"
				+ layoutParams.leftMargin + " right:"
				+ layoutParams.rightMargin);
		mTabContainerBg.setLayoutParams(layoutParams);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorMag.registerListener(sensorLis, gravitySensor,
				SensorManager.SENSOR_DELAY_UI);
		setShoppingBarEnable(true);
	}

	@Override
	protected BackKeyListener getBackKeyListener() {
		return this;
	}

	@Override
	public boolean shouldHackBackKey() {
		return FRAGMENT_TAG_CATEGORY.equals(mCurrentTag);
	}

	@Override
	public void onBackKeyFired() {
		onBackPressed();
	}

	@Override
	protected void onPause() {
		sensorMag.unregisterListener(sensorLis);
		super.onPause();
	}

	private SensorEventListener sensorLis = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
				return;
			}
			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			float absy = Math.abs(y);
			float absx = Math.abs(x);
			if (absx > 5 && absy < 5 && mScreenStatus
					&& FRAGMENT_TAG_HOME.equals(mCurrentTag)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				mScreenStatus = false;
			}
			if (absy > 5 && absx < 5 && !mScreenStatus) {
				mScreenStatus = true;
			}
		}
	};

}
