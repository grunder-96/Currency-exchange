package com.edu.pet.exception;

public class CurrencyCodeUniqueException extends RuntimeException{

    public CurrencyCodeUniqueException() {
    }

    public CurrencyCodeUniqueException(String message) {
        super(message);
    }
}
