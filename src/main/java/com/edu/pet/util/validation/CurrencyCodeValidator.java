package com.edu.pet.util.validation;

public class CurrencyCodeValidator extends Validator<String> {

    @Override
    public boolean isValid(String s) {
        return !s.isBlank() && s.matches("[a-zA-Z]{3}");
    }
}