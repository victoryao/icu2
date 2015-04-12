
package com.xiaomi.xms.sales.util;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopIntentService;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.widget.BaseAlertDialog;

public class AppUpdater {
    public static final String TAG = "AppUpdater";

    private Handler mHandler;
    private Context mContext;
    private String mURL;

    public AppUpdater(Context context) {
        mHandler = new Handler();
        mContext = context;
    }

    public boolean needCheck() {
        long now = System.currentTimeMillis();
        // check every PERIOD_CHECK_UPDATE
        long lastCheck = Utils.Preference
                .getLongPref(mContext, Constants.AppUpdate.PREF_LAST_CHECK_UPDATE, 0);
        if (Math.abs(now - lastCheck) < Constants.AppUpdate.PERIOD_CHECK_UPDATE) {
            return false;
        }
        Utils.Preference.setLongPref(mContext, Constants.AppUpdate.PREF_LAST_CHECK_UPDATE, now);

        // when last check update is ok, no check in PERIOD_CHECK_UPDATE mills
        long lastUpdate = Utils.Preference.getLongPref(mContext,
                Constants.AppUpdate.PREF_LAST_UPDATE_IS_OK, 0);
        if (Math.abs(now - lastUpdate) < Constants.AppUpdate.PERIOD_UPDATE_OK) {
            return false;
        }
        return true;
    }

    public void sendCheckApkUpdateService(boolean force) {
        if (force) {
            sendCheckApkUpdateService();
            return;
        }
        if (!needCheck()) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCheckApkUpdateService();
            }
        }, 10 * 1000);
    }

    // start check service, get new version url
    public void sendCheckApkUpdateService() {
        if (mContext == null) {
            return;
        }
        Intent intent = new Intent(mContext, ShopIntentService.class);
        intent.setAction(Constants.Intent.ACTION_CHECK_UPDATE);
        mContext.startService(intent);
    }

    // download using DownloadManager
    public void download(String url) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.show(mContext, R.string.update_no_sd);
            return;
        }
        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new Request(Uri.parse(url));
        request.setShowRunningNotification(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Device.PACKAGE
                + "_" + System.currentTimeMillis() + Constants.AppUpdate.FILE_SUFFIX_APK);
        request.setTitle(mContext.getResources().getString(R.string.app_name));
        request.setDescription(mContext.getResources().getString(R.string.self_confirm_dowloading));
        long id = dm.enqueue(request);
        Utils.Preference.setLongPref(mContext, Constants.AppUpdate.PREF_DOWNLOAD_ID, id);
    }

    public void loadVersionLogAndPopDialog(String version, String url, String updateSummary) {
        mURL = url;
        LogUtil.d(TAG, "popup dialog:" + version);
        final BaseAlertDialog dialog = new BaseAlertDialog(mContext);
        dialog.setCancelable(false);
        dialog.setTitle(mContext.getString(R.string.update_title) + "：" + version);
        dialog.setMessage(updateSummary);
        dialog.setPositiveButton(R.string.immediately_update, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(mURL);
            }
        });
//        dialog.setNegativeButton(R.string.cancel_update, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) {
                    return;
                }
                if (!((BaseActivity) mContext).isFinishing()) {
                    dialog.show();
                }
            }
        }, 1000);
    }

    public static class DownloadCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                // get download id
                long id = Utils.Preference.getLongPref(context,
                        Constants.AppUpdate.PREF_DOWNLOAD_ID, 0);
                long downloadid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                if (downloadid == id) {
                    downloadCompleted(context, downloadid);
                }
            }
        }

        private void downloadCompleted(Context context, long id) {
            String localApkUri = getDownloadFileById(context, id);
            if (!TextUtils.isEmpty(localApkUri)) {
                Intent installIntent = new Intent();
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setAction(Intent.ACTION_VIEW);
                installIntent.setDataAndType(Uri.parse(localApkUri),
                        Constants.AppUpdate.FILE_TYPE_APK);
                context.startActivity(installIntent);
            }
        }

        private String getDownloadFileById(Context context, long id) {
            final DownloadManager dm = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);

            // query by downloadid
            Query query = new Query();
            query.setFilterById(id);
            Cursor c = dm.query(query);
            if (c == null) {
                return null;
            }

            String localApkUri = null;
            try {
                int indexStatus = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int indexLocalURI = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                if (c.moveToFirst()) {
                    boolean failed = false;
                    do {
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(indexStatus)) {
                            localApkUri = c.getString(indexLocalURI);
                            break;
                        }
                        if (DownloadManager.STATUS_FAILED == c.getInt(indexStatus)) {
                            failed = true;
                            break;
                        }
                    } while (c.moveToNext());
                    if (failed) {
                        notifyDownloadFailed(context);
                    }
                }
            } finally {
                c.close();
            }
            return localApkUri;
        }

        private void notifyDownloadFailed(Context context) {
            String title = context.getString(R.string.download_failed_title);
            String content = context.getString(R.string.download_failed_tips);
            Intent intent = new Intent();
            intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);

            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.app_icon, title,
                    System.currentTimeMillis());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setLatestEventInfo(context, title, content, pendingIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_SOUND;
            nm.notify(R.string.download_failed_id, notification);
        }
    }
}
