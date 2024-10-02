package com.edu.pet.util.validation;

import java.util.Currency;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CurrencyCodeValidator {

    private static Set<String> codes;

    private CurrencyCodeValidator() {

    }

    public static boolean isValid(String code) {
         if (Objects.isNull(codes)) {
             codes = Currency.getAvailableCurrencies().stream()
                     .map(Currency::getCurrencyCode)
                     .collect(Collectors.toSet());
         }
         return codes.contains(code.toUpperCase());
    }
}