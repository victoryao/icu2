
package com.xiaomi.xms.sales.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.loader.ImageLoader;
import com.xiaomi.xms.sales.model.Image;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SplashUtil {

    private static final String IMAGE_PATH = Environment.getExternalStorageDirectory() + "/mishop/";
    private static final String FILENAME = "splash.jpg";

    private static final String JSON_TAG_START = "start";
    private static final String JSON_TAG_END = "end";
    private static final String JSON_TAG_URL = "url";

    public static Bitmap getSplashImage() {
        Bitmap bmp = null;
        if (isSplashValid()) {
            try {
                bmp = BitmapFactory.decodeFileDescriptor(new FileInputStream(IMAGE_PATH + FILENAME).getFD());
            } catch (Exception e) {
                Utils.Preference.removePref(ShopApp.getContext(),
                        Constants.Prefence.PREF_KEY_SPLASH_INFO);
            }
        }
        new LoadInfoThread().start();
        return bmp;
    }

    private static boolean isSplashValid() {
        String info = Utils.Preference.getStringPref(ShopApp.getContext(),
                Constants.Prefence.PREF_KEY_SPLASH_INFO, "");
        if (TextUtils.isEmpty(info)) {
            return false;
        }
        if (!new File(IMAGE_PATH + FILENAME).exists()) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(info);
            String start = json.getString(JSON_TAG_START);
            String end = json.getString(JSON_TAG_END);
            long s = Long.parseLong(start);
            long e = Long.parseLong(end);
            long now = System.currentTimeMillis() / 1000;
            if (now >= s && now <= e) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static class LoadInfoThread extends Thread {

        @Override
        public void run() {
            try {
                Request r = new Request(HostManager.getSplash());
                if (r.getStatus() == Request.STATUS_OK) {
                    JSONObject json = r.requestJSON().getJSONObject("data");
                    if (!json.toString().equals(
                            Utils.Preference.getStringPref(ShopApp.getContext(),
                                    Constants.Prefence.PREF_KEY_SPLASH_INFO, ""))) {
                        new LoadImageThread(json).start();
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private static class LoadImageThread extends Thread {
        private JSONObject mJson;
        private String mImageUrl;

        public LoadImageThread(JSONObject json) {
            mJson = json;
            try {
                mImageUrl = json.getString(JSON_TAG_URL);
            } catch (JSONException e) {
            }
        }

        private boolean saveFile(Bitmap bm, String path, String fileName) {
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(path + fileName);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                        myCaptureFile));
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            Image image = new Image(mImageUrl);
            Bitmap bitmap = ImageLoader.getInstance().syncLoadLocalImage(image, true);
            if (bitmap == null) {
                return;
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            if (saveFile(bitmap, IMAGE_PATH, FILENAME)) {
                Utils.Preference.setStringPref(ShopApp.getContext(),
                        Constants.Prefence.PREF_KEY_SPLASH_INFO, mJson.toString());
            }
            bitmap.recycle();
            bitmap = null;
        }
    }

}
