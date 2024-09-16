package com.edu.pet.exception;

public class CurrencyCodeInvalidException extends RuntimeException {

    public CurrencyCodeInvalidException() {

    }

    public CurrencyCodeInvalidException(String message) {
        super(message);
    }
}
