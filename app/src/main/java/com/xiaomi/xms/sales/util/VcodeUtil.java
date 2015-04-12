
package com.xiaomi.xms.sales.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.loader.RequestLoader;
import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.model.Tags;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONObject;

public class VcodeUtil {
    // LoaderManager用来区别不同的Loader
    private static final int REQUEST_LOADER = 0;
    private static final int REQUEST_LOADER_VERIFYVCODE = 1;
    // RequestLoader用来区别不同的Request
    private static final int REQUEST_CODE_LOAD_IMAGE = 0;
    private static final int REQUEST_CODE_LOAD_IMAGE_VERIFYVCODE = 1;
    private RequestLoader mFetchVcodeLoader;
    private RequestLoader mVerifyVcodeLoader;
    private Context mContext;
    private Fragment mFragment;

    public VcodeUtil(Fragment mFragment) {
        super();
        this.mContext = mFragment.getActivity();
        this.mFragment = mFragment;
    }

    public interface FetchVcodeCallbackInterface {
        void vCodeUrl(String urlPath);
    }

    public void fetchVerifyCode(final FetchVcodeCallbackInterface fetchVcodeCallback) {
        mFragment.getLoaderManager().initLoader(REQUEST_LOADER, null, new LoaderCallbacks<RequestLoader.Result>() {

            @Override
            public Loader<Result> onCreateLoader(int id, Bundle bundle) {
                mFetchVcodeLoader = new RequestLoader(mContext);
                return mFetchVcodeLoader;
            }

            @Override
            public void onLoadFinished(Loader<Result> loader, Result data) {
                if (data != null) {
                    if (REQUEST_CODE_LOAD_IMAGE == data.mRequestCode) {
                        // 每一次调用结束，必须destroyLoader
                        mFragment.getLoaderManager().destroyLoader(REQUEST_LOADER);
                        String url = "";
                        if (data.mStatus == Request.STATUS_OK) {
                            JSONObject json = data.mData;
                            if (json != null) {
                                if (Tags.isJSONResultOK(json)) {
                                    JSONObject dataJson = json.optJSONObject(Tags.DATA);
                                    if (dataJson != null) {
                                        url = dataJson.optString(Tags.CheckCode.URL);
                                    } else {
                                        ToastUtil.show(mContext, R.string.data_error);
                                    }
                                } else {
                                    ToastUtil.show(mContext, json.optJSONObject(Tags.DATA).optString(Tags.DESCRIPTION));
                                }
                            }
                        }
                        fetchVcodeCallback.vCodeUrl(url);
                    }
                }

            }

            @Override
            public void onLoaderReset(Loader<Result> loader) {

            }
        });
        if (mFetchVcodeLoader != null) {
            Request request = new Request(HostManager.getCheckVCode());
            request.addParam(HostManager.Parameters.Keys.CHECKCODE_TYPE,
                    HostManager.Parameters.Values.CHECKCODE_TYPE_GET);
            mFetchVcodeLoader.load(REQUEST_CODE_LOAD_IMAGE, request);
        }
    }

    public interface VerifyVcodeCallbackInterface {
        void verifyResult(boolean verifyResult);
    }

    public void verifyVCode(String vCode, final VerifyVcodeCallbackInterface verifyVcodeCallback) {
        mFragment.getLoaderManager().initLoader(REQUEST_LOADER_VERIFYVCODE, null,
                new LoaderCallbacks<RequestLoader.Result>() {

                    @Override
                    public Loader<Result> onCreateLoader(int id, Bundle bundle) {
                        mVerifyVcodeLoader = new RequestLoader(mContext);
                        return mVerifyVcodeLoader;
                    }

                    @Override
                    public void onLoadFinished(Loader<Result> loader, Result data) {
                        if (data != null) {
                            if (REQUEST_CODE_LOAD_IMAGE_VERIFYVCODE == data.mRequestCode) {
                                boolean verifyResult = false;
                                // 每一次调用结束，必须destroyLoader
                                mFragment.getLoaderManager().destroyLoader(REQUEST_LOADER_VERIFYVCODE);
                                if (data.mStatus == Request.STATUS_OK) {
                                    JSONObject json = data.mData;
                                    if (json != null) {
                                        if (Tags.isJSONResultOK(json)) {
                                            verifyResult = true;
                                        }
                                    }
                                }
                                verifyVcodeCallback.verifyResult(verifyResult);
                            }
                        }

                    }

                    @Override
                    public void onLoaderReset(Loader<Result> loader) {

                    }
                });
        if (mVerifyVcodeLoader != null) {
            Request request = new Request(HostManager.getCheckVCode());
            request.addParam(HostManager.Parameters.Keys.CHECKCODE_TYPE,
                    HostManager.Parameters.Values.CHECKCODE_TYPE_CHECK);
            request.addParam(HostManager.Parameters.Keys.CHECKCODE_CODE, vCode);
            mVerifyVcodeLoader.load(REQUEST_CODE_LOAD_IMAGE_VERIFYVCODE, request);
        }
    }
}
