package com.xiaomi.xms.sales.xmsf.account.exception;

public class InvalidCredentialException extends CloudServiceException {

    public InvalidCredentialException(String detailMessage) {
        super(detailMessage);
    }
}
