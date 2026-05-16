package com.graduation.smarty_commerce.Exceptions;

public class ProductServiceException extends RuntimeException {
    private static final long serialVersionUID = 5313493413859549877L;

    public ProductServiceException(String message) {
        super(message);
    }
}
