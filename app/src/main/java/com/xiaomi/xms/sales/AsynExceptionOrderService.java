package com.xiaomi.xms.sales;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;

import com.xiaomi.xms.sales.misc.BASE64Encoder;
import com.xiaomi.xms.sales.model.PosHistory;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Device;
import com.xiaomi.xms.sales.util.JsonUtil;
import com.xiaomi.xms.sales.util.MD5Utils;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.xmsf.account.LoginManager;

public class AsynExceptionOrderService extends Service {

	final Handler asynExceptionOrderHandler = new Handler();
	AsynExceptionOrderTask asynExceptionOrderTask;

	@Override
	public void onCreate() {

		asyncExceptionOrderEach10min();

	}
	



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		asynExceptionOrderHandler.postDelayed(asyncExceptionOrderOnceRunnable, 5 * 1000);
		return super.onStartCommand(intent, flags, startId);
	}




	private void asyncExceptionOrderEach10min() {
		asynExceptionOrderHandler.postDelayed(asyncExceptionOrderRunnable, 1 * 60 * 1000);

	}

	Runnable asyncExceptionOrderOnceRunnable = new Runnable() {
		@Override
		public void run() {
			ArrayList<PosHistory> posHistoryList = getPosHistoryList();
			for(PosHistory posHistory:posHistoryList){
				asynExceptionOrderTask = new AsynExceptionOrderTask();
				asynExceptionOrderTask.execute(posHistory);
			}
		}

	};
	
	Runnable asyncExceptionOrderRunnable = new Runnable() {
		@Override
		public void run() {
			ArrayList<PosHistory> posHistoryList = getPosHistoryList();
			for(PosHistory posHistory:posHistoryList){
				asynExceptionOrderTask = new AsynExceptionOrderTask();
				asynExceptionOrderTask.execute(posHistory);
			}
			
			asynExceptionOrderHandler.postDelayed(asyncExceptionOrderRunnable, 10 * 60 * 1000);
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		System.out.println("services destroy .........");

	}

	class AsynExceptionOrderTask extends AsyncTask<PosHistory, Integer, Integer> {
		PosHistory posHistory;
		@Override
		protected Integer doInBackground(PosHistory... params) {
			posHistory = params[0];
			int code = 0;
			Request request = new Request(HostManager.URL_XMS_SALE_API);
			 JSONObject jsonObject = new JSONObject();
			try {
				String mUserId = LoginManager.getInstance().getUserId() == null ? posHistory.getmUserId() : LoginManager.getInstance().getUserId();
				jsonObject.put(Tags.XMSAPI.USERID, mUserId);
				jsonObject.put(Tags.XMSAPI.ORGID,
				        Utils.Preference.getStringPref(ShopApp.getContext(), Constants.Account.PREF_USER_ORGID, ""));
				jsonObject.put(Tags.XMSAPI.IMEI, Device.IMEI);
				jsonObject.put("serviceNumber", posHistory.getmOrderId());
				jsonObject.put("info", posHistory.getInfo());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
            String data = JsonUtil.creatRequestJson(HostManager.Method.METHOD_SAVEPAYINFO, jsonObject);
			if (!TextUtils.isEmpty(data)) {
				request.addParam(Tags.RequestKey.DATA, data);
			}

			int status = request.getStatus();
			if (status == Request.STATUS_OK) {
				JSONObject mainObject = request.requestJSON();
				try {
					code = fromJSONObject(mainObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return code;
		}

		protected Integer fromJSONObject(JSONObject json) throws JSONException {
			
			int code = json.optJSONObject(Tags.HEADER).optInt(Tags.CODE);
//			String desc = json.getJSONObject(Tags.HEADER).optString(Tags.DESC);
			return code;
		}

		@Override
		protected void onPostExecute(Integer code) {
			ArrayList<PosHistory> posHistoryList = getPosHistoryList();
			if(posHistoryList == null){
				return;
			}
			if(code == 200 || code == 303){
				posHistoryList.remove(posHistory);
			}else{
				for(int i = posHistoryList.size()-1;i>=0;i--){
					if(posHistoryList.get(i).getInfo() == null){
						posHistoryList.remove(i);
					}
				}
				
			}
			savePosHistoryList(posHistoryList);
		}

		public String creatRequestJson(String requestMethod, String body) {
			String requestJson = "";
			JSONObject jsonObject = new JSONObject();
			try {
				JSONObject headJsonObj = new JSONObject();
				headJsonObj.put(Tags.RequestKey.APPID, Tags.RequestValue.APPID);
				headJsonObj.put(Tags.RequestKey.KEY, Tags.RequestValue.KEY);
				String sign = MD5Utils.getMD5(Tags.RequestValue.APPID + body + Tags.RequestValue.KEY).toUpperCase(Locale.getDefault());
				headJsonObj.put(Tags.RequestKey.SIGN, sign);
				headJsonObj.put(Tags.RequestKey.URL, "");
				headJsonObj.put(Tags.RequestKey.METHOD, requestMethod);
				headJsonObj.put(Tags.RequestKey.OPERATORID, "");
				headJsonObj.put(Tags.RequestKey.OPERATORMIHOME, "");
				headJsonObj.put(Tags.RequestKey.APITYPE, Tags.RequestValue.APITYPE);
				jsonObject.put(Tags.RequestKey.HEADER, headJsonObj);
				jsonObject.put(Tags.RequestKey.BODY, body);
			} catch (JSONException e) {
				return requestJson;
			}
			requestJson = jsonObject.toString();
			if (!TextUtils.isEmpty(requestJson)) {
				String base64 = encrypt(requestJson);
				base64 = base64.trim().replace("\\r", " ").replace("\\n", " ");
				return base64;
			}
			return requestJson;
		}

		/**
		 * base64加密处理
		 * 
		 * @param key
		 * @return
		 */
		public String encrypt(String key) {
			return (new BASE64Encoder()).encodeBuffer(key.getBytes());
		}

	}
	
	
	  private ArrayList<PosHistory> getPosHistoryList(){
			ArrayList<PosHistory> posHistoryListObject = new ArrayList<PosHistory>();
			SharedPreferences pmySharedPreferences = getSharedPreferences(Constants.posHistoryCache, Activity.MODE_PRIVATE);
			if(pmySharedPreferences != null){
				String posHistoryList = null;
				if(pmySharedPreferences != null && pmySharedPreferences.getAll() != null && pmySharedPreferences.getAll().size() > 0){
					posHistoryList = pmySharedPreferences.getString("posHistoryCache", null);
				}
				if(posHistoryList != null){
						String pproductBase64 = posHistoryList;
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
								posHistoryListObject = (ArrayList<PosHistory>) ois.readObject();
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
			return posHistoryListObject;
		}

	  
	   private void savePosHistoryList(ArrayList<PosHistory> posHistory) {
		   
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(posHistory);
			} catch (IOException e) {
				e.printStackTrace();
			}

			SharedPreferences mySharedPreferences = getSharedPreferences(Constants.posHistoryCache, Activity.MODE_PRIVATE);
			// 将Product对象转换成byte数组，并将其进行base64编码
			String productBase64 = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			// 将编码后的字符串写到base64.xml文件中
			editor.putString("posHistoryCache", productBase64);
			editor.commit();
		}
	   
}
