
package com.xiaomi.xms.sales.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.xiaomi.xms.sales.loader.RequestLoader.Result;
import com.xiaomi.xms.sales.request.Request;

import org.json.JSONObject;

public class RequestLoader extends AsyncTaskLoader<Result> {
    private static final String TAG = "RequestLoader";

    private Request mRequest;
    private int mRequestCode;

    public RequestLoader(Context context) {
        super(context);
    }

    public void load(int requestCode, Request req) {
        mRequestCode = requestCode;
        mRequest = req;
        forceLoad();
    }

    @Override
    public Result loadInBackground() {
        if (mRequest != null) {
            Result result = new Result();
            result.mRequestCode = mRequestCode;
            result.mStatus = mRequest.getStatus();
            result.mData = mRequest.requestJSON();
            result.mEtag = mRequest.getEtag();
            result.mRequest = mRequest;
            return result;
        }
        return null;
    }

    public static final class Result extends BaseResult {
        public int mRequestCode;
        public int mStatus;
        public String mRequestUrl;
        public JSONObject mData;
        public String mEtag;
        public Request mRequest;

        @Override
        public BaseResult shallowClone() {
            Result newResult = new Result();
            newResult.mRequestCode = mRequestCode;
            newResult.mStatus = mStatus;
            newResult.mRequestUrl = mRequestUrl;
            newResult.mData = mData;
            newResult.mEtag = mEtag;
            newResult.mRequest = mRequest;
            return newResult;
        }

        @Override
        protected int getCount() {
            return 0;
        }

    }
}
