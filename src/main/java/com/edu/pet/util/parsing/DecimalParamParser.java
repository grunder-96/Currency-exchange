package com.edu.pet.util.parsing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DecimalParamParser {

    private DecimalParamParser() {

    }

    public static BigDecimal parse(String input) {
        try {
            BigDecimal rate = new BigDecimal(input).setScale(6, RoundingMode.HALF_DOWN);
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("%s must be greater than zero");
            }
            return rate;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("%s is not valid");
        }
    }
}
