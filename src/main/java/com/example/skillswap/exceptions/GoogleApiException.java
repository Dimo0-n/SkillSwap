package com.example.skillswap.exceptions;

import org.springframework.http.HttpStatus;

public class GoogleApiException extends ApiException {

    public GoogleApiException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
