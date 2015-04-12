package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.activity.SameDayReturnActivity;
import com.xiaomi.xms.sales.adapter.BluetoothListAdapter;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.util.ToastUtil;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;
import com.yeepay.bluetooth.pos.bluetooth.BluetoothBroadcastReceiver.OnBluetoothReceiverListener;
import com.yeepay.bluetooth.pos.bluetooth.BluetoothHelper;
import com.yeepay.bluetooth.pos.controll.IController;
import com.yeepay.bluetooth.pos.controll.Me31Controller;
import com.yeepay.bluetooth.pos.user.OnBluetoothConnectCallback;

public class ReturnBluetoothFragment extends BaseFragment implements OnBluetoothReceiverListener {
	private static final String TAG = "BluetoothFragment";

	private TextView mBluetoothTitle;
	private BaseListView mListView;
	private EmptyLoadingView mLoadingView;
	private BluetoothListAdapter mAdapter;
	private BluetoothHelper mBluetoothHelper;
	private ArrayList<BluetoothDevice> mBluetoothDevices;
	private ProgressDialog mProgressDialog;
	private Bundle mBundle;
	private String mBluetoothName;
	private Button mBluetoothSearchBtn;
	private View mBlueInfoView;
	private ViewStub mBluetoothCloseViewStub;
	private int orderType;
	private IController mcontroller;
	private String mDeviceName;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.bluetooth_fragment, container, false);
		mBluetoothTitle = (TextView) view.findViewById(R.id.bluetooth_list_title);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mBluetoothSearchBtn = (Button) view.findViewById(R.id.bluetooth_search_btn);
		mBlueInfoView = view.findViewById(R.id.bluetooth_container);
		mBluetoothCloseViewStub = (ViewStub) view.findViewById(R.id.bluetooth_close_stub);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.bluetooth_list_empty);

		mAdapter = new BluetoothListAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);

		mBluetoothHelper = BluetoothHelper.getInstance(getActivity(), this);
		mBluetoothDevices = new ArrayList<BluetoothDevice>();
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setCancelable(false);
		mDeviceName = Utils.Preference.getStringPref(getActivity(), Constants.SameDayReturn.PREF_KEY_ORDER_DEVICE_NAME, "");
	
		mcontroller = Me31Controller.getInstance();
		handleIntent();

		// 蓝牙没打开，提示
		if (!mBluetoothHelper.isEnable()) {
			mBlueInfoView.setVisibility(View.GONE);
			mBluetoothSearchBtn.setVisibility(View.GONE);
			ToastUtil.show(getActivity(), R.string.bluetooth_close_info);
			if (mBluetoothCloseViewStub != null) {
				View inflatedView = mBluetoothCloseViewStub.inflate();
				Button bluetoothBtn = (Button) inflatedView.findViewById(R.id.button);
				bluetoothBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});
				return view;
			}
		}
		setPairedListView();

		

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBluetoothSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBluetoothHelper.isEnable()) {
					mBluetoothDevices.clear();
					mBluetoothHelper.start();
					mAdapter.updateData(mBluetoothDevices);
				}
			}
		});
	}

	private void handleIntent() {
		mBundle = getArguments();
		if (mBundle != null) {
			orderType = mBundle.getInt(Constants.Intent.EXTRA_ORDER_TYPE);
		}
	}

	private void setPairedListView() {
		// 获取已经绑定过的设备列表
		mBluetoothDevices.clear();
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		int size = 0;
		if (bluetoothAdapter != null && bluetoothAdapter.getBondedDevices() != null) {
			size = bluetoothAdapter.getBondedDevices().size();
		}
		if (size > 0) {
			for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
				if (device.getName().equals(mDeviceName)) {
					mBluetoothDevices.add(device);
				}
			}
			if (mBluetoothTitle.getVisibility() == View.GONE) {
				mBluetoothTitle.setVisibility(View.VISIBLE);
				mBluetoothTitle.setText(R.string.bluetooth_paired_list_info);
			}
			mAdapter.updateData(mBluetoothDevices);
		} else {
			mBluetoothHelper.start();
			mAdapter.updateData(mBluetoothDevices);
		}
	}

	private OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
			BluetoothDevice bluetoothDevice = (BluetoothDevice) item.getTag();
			if (bluetoothDevice != null) {
				mBluetoothName = bluetoothDevice.getName();
				Utils.Preference.setStringPref(getActivity(), Constants.Prefence.PREF_CONNECTED_BLUETOOTH_DEVICE, bluetoothDevice.getAddress());
				try {
					LogHelper.getInstance(getActivity()).save("", Constants.LogType.CONNECTING_POS_DEVICE, "", "");
				} catch (Exception e) {
					e.printStackTrace();
				}
				mcontroller.connect(getActivity(), bluetoothDevice.getAddress(), mBluetoothConnectCallback);
				Utils.Preference.setStringPref(getActivity(), Constants.Account.PREF_POS_MAC_ADDRESS, bluetoothDevice.getAddress());
			}
		}
	};

	// 蓝牙开始搜索回调
	@Override
	public void onStartDiscovery() {
		LogUtil.i(TAG, "bluetooth start discovery");
		mProgressDialog.setMessage("正在搜索蓝牙设备...");
		mProgressDialog.show();
		try {
			LogHelper.getInstance(getActivity()).save("", Constants.LogType.START_BLUETOOTH_DISCOVERY, "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 蓝牙状态改变回调
	@Override
	public void onStateChange(int state) {
		LogUtil.i(TAG, "bluetooth state = " + state);
		switch (state) {
		case Constants.Bluetooth.STATE_ON:
			if (mBluetoothCloseViewStub != null) {
				mBlueInfoView.setVisibility(View.VISIBLE);
				mBluetoothSearchBtn.setVisibility(View.VISIBLE);
				mBluetoothCloseViewStub.setVisibility(View.GONE);
				setPairedListView();
			}
			break;
		case Constants.Bluetooth.STATE_OFF:

			break;
		default:
			break;
		}
	}

	// 蓝牙搜索到设备回调
	@Override
	public void onFound(BluetoothDevice device) {
		LogUtil.i(TAG, "bluetooth found device, name = " + device.getName() + " mac address = " + device.getAddress());

		if (!mBluetoothDevices.contains(device)) {
			if (!TextUtils.isEmpty(device.getName()) && device.getName().equals(mDeviceName)) {
				mBluetoothDevices.add(device);
				mAdapter.notifyDataSetChanged();
				if (mBluetoothTitle.getVisibility() == View.GONE) {
					mBluetoothTitle.setVisibility(View.VISIBLE);
				}
				mBluetoothTitle.setText(R.string.bluetooth_search_list_info);
			}
		}
	}

	// 蓝牙结束搜索回调
	@Override
	public void onFinishDiscovery() {
		LogUtil.i(TAG, "bluetooth finish discovery");
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		if (mBluetoothDevices != null && mBluetoothDevices.size() > 0) {
			mAdapter.notifyDataSetChanged();
		} else {
			mLoadingView.stopLoading(false);
		}
		try {
			LogHelper.getInstance(getActivity()).save("", Constants.LogType.START_BLUETOOTH_DISCOVERY_FINISH, "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OnBluetoothConnectCallback mBluetoothConnectCallback = new OnBluetoothConnectCallback() {

		@Override
		public void onConnectStar() {
			LogUtil.i(TAG, "onConnectStar");
			try {
				LogHelper.getInstance(getActivity()).save("", Constants.LogType.START_CONNECTING_BLUETOOTH_DEVICE, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog.setMessage("正在连接蓝牙设备...");
			mProgressDialog.show();
		}

		@Override
		public void onConnectLost(Throwable arg0) {
			LogUtil.i(TAG, "onConnectLost");
			try {
				LogHelper.getInstance(getActivity()).save("", Constants.LogType.CONNECTING_BLUETOOTH_DEVICE_LOST, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog.dismiss();
		}

		@Override
		public void onConnectFailed() {
			LogUtil.i(TAG, "onConnectFailed");
			try {
				LogHelper.getInstance(getActivity()).save("", Constants.LogType.CONNECTING_BLUETOOTH_DEVICE_FAILED, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			ToastUtil.show(getActivity(), R.string.bluetooth_connect_error_info);
		}

		@Override
		public void onConnectEnd() {
			LogUtil.i(TAG, "onConnectEnd");
			try {
				LogHelper.getInstance(getActivity()).save("", Constants.LogType.CONNECTING_BLUETOOTH_DEVICE_END, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog.dismiss();
		}

		@Override
		public void onConnectSuccess() {
			LogUtil.i(TAG, "onConnectSuccess");
			try {
				LogHelper.getInstance(getActivity()).save("", Constants.LogType.CONNECTING_BLUETOOTH_DEVICE_SUCCESS, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			SameDayReturnActivity father = (SameDayReturnActivity) getActivity();
			mBundle.putString(Constants.Intent.EXTRA_RETURN_SWITCH_TO_RETURN, "returnOrder");
			// mBundle.putInt(Constants.Intent.EXTRA_PRINTER_ID, printerId);
			father.showFragment(SameDayReturnActivity.Fragments.TAG_RETURN_CONFIRM_FRAGMENT, mBundle, true);
			
			 
		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LogUtil.i(TAG, "onDestroyView");
		mBluetoothHelper.release();
		mBluetoothHelper = null;
	}

	
}
