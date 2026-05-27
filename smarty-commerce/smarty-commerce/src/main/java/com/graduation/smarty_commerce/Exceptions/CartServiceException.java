package com.graduation.smarty_commerce.Exceptions;

public class CartServiceException extends RuntimeException {

    private static final long serialVersionUID = 3456789012345678901L;

    public CartServiceException(String message) {
        super(message);
    }
}

