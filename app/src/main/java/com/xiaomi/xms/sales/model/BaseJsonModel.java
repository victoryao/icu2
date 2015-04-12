
package com.xiaomi.xms.sales.model;

public abstract class BaseJsonModel {
    protected String mResult;

    public String getResult() {
        return mResult;
    }

    public void setResult(String result) {
        this.mResult = result;
    }

    protected String mDescription;
    protected int mCode;
    protected boolean mNoJson;

    public boolean getNoJson() {
        return mNoJson;
    }

    public void setNoJson(boolean noJson) {
        this.mNoJson = noJson;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }
}
