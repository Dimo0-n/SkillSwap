package com.example.skillswap.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RuntimeException{
    private String message;

    private HttpStatus status = HttpStatus.NOT_FOUND;

    public NotFoundException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
