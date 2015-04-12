package com.xiaomi.xms.sales.loader;

import android.os.Handler.Callback;

import com.xiaomi.xms.sales.loader.BaseResult.ResultStatus;


public interface ProgressNotifiable {
    /**
     * 每一阶段的载入开始时均会调用该方法
     */
    public void startLoading(boolean hasData);

    /**
     * 每一阶段的载入结束时均会调用该方法
     */
    public void stopLoading(boolean hasData);

    /**
     * 绑定时马上会被调用，用于初始化当前的载入状态
     */
    public void init(boolean hasData, boolean isLoading);

    /**
     * 当发生错误时，调用该方法，用于告诉用户当前状态
     */
    public void onError(boolean hasData, ResultStatus status, Callback callback);
}
