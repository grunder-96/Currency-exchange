package com.edu.pet.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RateDto {

    private int id;
    private CurrencyDto baseCurrency;
    private CurrencyDto targetCurrency;
    private BigDecimal rate;

    public RateDto() {}

    public RateDto(int id, CurrencyDto baseCurrency, CurrencyDto targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CurrencyDto getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(CurrencyDto baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public CurrencyDto getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(CurrencyDto targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate.setScale(2, RoundingMode.HALF_UP);
    }
}