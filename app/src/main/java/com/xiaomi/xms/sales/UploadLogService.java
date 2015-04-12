package com.xiaomi.xms.sales;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.xiaomi.xms.sales.misc.BASE64Encoder;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.LogHelper;
import com.xiaomi.xms.sales.util.MD5Utils;

public class UploadLogService extends Service {

	final Handler uploadHandler = new Handler();
	UploadLogFileTask uploadLogFileTask;

	@Override
	public void onCreate() {

		uploadLogEach2Hours();

	}
	



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uploadHandler.postDelayed(asyncOnceUploadRunnable, 5 * 1000);
		return super.onStartCommand(intent, flags, startId);
	}




	private void uploadLogEach2Hours() {
		uploadHandler.postDelayed(asyncUploadRunnable, 5 * 60 * 1000);

	}

	Runnable asyncOnceUploadRunnable = new Runnable() {
		@Override
		public void run() {
			uploadLogFileTask = new UploadLogFileTask();
			uploadLogFileTask.execute();
		}

	};
	
	Runnable asyncUploadRunnable = new Runnable() {
		@Override
		public void run() {
			uploadLogFileTask = new UploadLogFileTask();
			uploadLogFileTask.execute();
			uploadHandler.postDelayed(asyncUploadRunnable, 1* 60 * 60 * 1000);
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

	class UploadLogFileTask extends AsyncTask<Object, Integer, String> {
		File file;

		@Override
		protected String doInBackground(Object... params) {
			String desc = "";
			Request request = new Request(HostManager.URL_M_XMS_SALE_API);
			file = LogHelper.init(getApplicationContext());
			String logContent = "";
			if (!file.exists()) {
				uploadLogFileTask.cancel(true);
				return null;
			}

			try {
				logContent = LogHelper.readLine(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// jsonObject.put(Tags.RequestKey.DATA, logContent);
			String data = creatRequestJson(HostManager.Method.METHOD_UPLOAD_LOG, logContent);
			if (!TextUtils.isEmpty(data)) {
				request.addParam(Tags.RequestKey.DATA, data);
			}

			int status = request.getStatus();
			if (status == Request.STATUS_OK) {
				JSONObject mainObject = request.requestJSON();
				try {
					desc = fromJSONObject(mainObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return desc;
		}

		protected String fromJSONObject(JSONObject json) throws JSONException {
			int code = json.optJSONObject(Tags.HEADER).optInt(Tags.CODE);
			String desc = json.getJSONObject(Tags.HEADER).optString(Tags.DESC);
			return desc;
		}

		@Override
		protected void onPostExecute(String result) {
			if(result.equals("ok")){
				try {
					LogHelper.cleanFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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

}
