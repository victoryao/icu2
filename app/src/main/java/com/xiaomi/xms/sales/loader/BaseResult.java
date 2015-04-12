
package com.xiaomi.xms.sales.loader;

import com.xiaomi.xms.sales.R;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseResult {
    public enum ResultStatus {
        NETWROK_ERROR, // 网络错误
        SERVICE_ERROR, // 服务错误
        DATA_ERROR, // 数据错误
        AUTH_ERROR, // 认证错误
        IP_ERROR, //IP不合法
        OK // 没有任何问题
    }

    private static Map<ResultStatus, Integer> RESULT_STATUS_DES = new HashMap<BaseResult.ResultStatus, Integer>();
    static {
        RESULT_STATUS_DES.put(ResultStatus.NETWROK_ERROR, R.string.network_unavaliable);
        RESULT_STATUS_DES.put(ResultStatus.SERVICE_ERROR, R.string.service_unavailiable);
        RESULT_STATUS_DES.put(ResultStatus.DATA_ERROR, R.string.data_error);
        RESULT_STATUS_DES.put(ResultStatus.AUTH_ERROR, R.string.auth_error);
        RESULT_STATUS_DES.put(ResultStatus.IP_ERROR, R.string.ip_error);
    }

    public static int getStatusDes(ResultStatus status) {
        return RESULT_STATUS_DES.get(status);
    }

    /**
     * 浅拷贝，用于刷新页面但不会重新加载数据
     */
    public abstract BaseResult shallowClone();

    private ResultStatus mResultStatus = ResultStatus.OK;

    /**
     * 必须返回真实的数量，如果数据是list类型，那么返回个数；如果数据是有无，那么 0代表无，非0代表有数据
     */
    protected abstract int getCount();

    public ResultStatus getResultStatus() {
        return mResultStatus;
    }

    public void setResultStatus(ResultStatus status) {
        mResultStatus = status;
    }
}
