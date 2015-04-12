package com.xiaomi.xms.sales.xmsf.account.exception;

public class InvalidResponseException extends CloudServiceException {

    public InvalidResponseException(String detailMessage) {
        super(detailMessage);
    }
}
