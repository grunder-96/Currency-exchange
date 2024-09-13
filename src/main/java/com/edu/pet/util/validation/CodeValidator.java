package com.edu.pet.util.validation;

public class CodeValidator extends Validator<String> {

    @Override
    public boolean validate(String s) {
        return !s.isBlank() && s.matches("[a-zA-Z]{3}");
    }
}