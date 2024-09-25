package com.edu.pet.exception;

public class NonExistsException extends RuntimeException {

    public NonExistsException() {

    }

    public NonExistsException(String message) {
        super(message);
    }
}
