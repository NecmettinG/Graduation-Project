package com.graduation.smarty_commerce.Exceptions;

public class OrderServiceException extends RuntimeException {

    private static final long serialVersionUID = 1234567890123456789L;

    public OrderServiceException(String message) {
        super(message);
    }
}

