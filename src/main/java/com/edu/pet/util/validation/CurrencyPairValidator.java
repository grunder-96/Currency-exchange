package com.edu.pet.util.validation;

public class CurrencyPairValidator {
    
    private CurrencyPairValidator() {
        
    }
    
    public static boolean isValid(String baseCurrencyCode, String targetCurrencyCode) {
        return CurrencyCodeValidator.isValid(baseCurrencyCode) &&
               CurrencyCodeValidator.isValid(targetCurrencyCode) &&
               !isCurrenciesSame(baseCurrencyCode, targetCurrencyCode);
    }
    
    public static boolean isValid(String currencyPair) {
        if (isMatches(currencyPair)) {
            return isValid(currencyPair.substring(0, 3), currencyPair.substring(3));
        }
        return false;
    }
    
    public static boolean isCurrenciesSame(String baseCurrencyCode, String targetCurrencyCode) {
        return baseCurrencyCode.equalsIgnoreCase(targetCurrencyCode);
    }
    
    public static boolean isCurrenciesSame(String currencyPair) {
        if (isMatches(currencyPair)) {
            return isCurrenciesSame(currencyPair.substring(0, 3), currencyPair.substring(3));
        }
        return false;
    }
    
    private static boolean isMatches(String currencyPair) {
        return currencyPair.matches("[a-zA-Z]{6}");
    }
}