package com.xiaomi.xms.sales.xmsf.account.data;

public class AccountInfo {
    private String userId;
    private String passToken;
    private String serviceToken;
    private String security;

    public AccountInfo(String userId, String passToken, String serviceToken, String security) {
        this.userId = userId;
        this.passToken = passToken;
        this.serviceToken = serviceToken;
        this.security = security;
    }

    public String getUserId() {
        return userId;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public String getPassToken() {
        return passToken;
    }

    public String getSecurity() {
        return security;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "userId='" + userId + '\'' +
                ", security='" + security + '\'' +
                '}';
    }
}
