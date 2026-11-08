package com.sizeok.common.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedOperationException extends SizeOkException {
    public AccessDeniedOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
}
