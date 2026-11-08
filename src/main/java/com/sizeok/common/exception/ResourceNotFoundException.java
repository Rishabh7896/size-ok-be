package com.sizeok.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SizeOkException {
    public ResourceNotFoundException(String resourceName, String id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
