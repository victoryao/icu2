package com.xiaomi.xms.sales.request;

import android.text.TextUtils;

public final class ExtendedAuthToken {

    private static final String SP = ",";

    public final String authToken;

    public final String security;

    private ExtendedAuthToken(String authToken, String security) {
        this.authToken = authToken;
        this.security = security;
    }

    public static ExtendedAuthToken build(String authToken,
            String security) {
        return new ExtendedAuthToken(authToken, security);
    }

    public static ExtendedAuthToken parse(String plain) {
        if (TextUtils.isEmpty(plain)) {
            return null;
        }
        String[] parts = plain.split(SP);
        if (parts.length != 2) {
            return null;
        }
        return new ExtendedAuthToken(parts[0], parts[1]);
    }

    public String toPlain() {
        return authToken + SP + security;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtendedAuthToken that = (ExtendedAuthToken) o;

        if (authToken != null ? !authToken.equals(that.authToken)
                : that.authToken != null) {
            return false;
        }
        if (security != null ? !security.equals(that.security)
                : that.security != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = authToken != null ? authToken.hashCode() : 0;
        result = 31 * result + (security != null ? security.hashCode() : 0);
        return result;
    }
}
