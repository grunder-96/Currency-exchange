package com.edu.pet.exception;

public class InternalErrorException extends RuntimeException {

    public InternalErrorException() {

    }

    public InternalErrorException(String message) {
        super(message);
    }
}