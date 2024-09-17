package com.edu.pet.exception;

public class AlreadyExistsException extends RuntimeException{

    public AlreadyExistsException() {
    }

    public AlreadyExistsException(String message) {
        super(message);
    }
}
