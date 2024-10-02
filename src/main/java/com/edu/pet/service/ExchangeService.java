package com.edu.pet.service;

import com.edu.pet.dao.ExchangeRateDao;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.Currency;
import com.edu.pet.model.Exchange;
import com.edu.pet.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExchangeService {

    private static final ExchangeService INSTANCE = new ExchangeService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private ExchangeService() {

    }

    public Exchange exchange(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws NonExistsException, InternalErrorException {
        Optional<ExchangeRate> maybeExchangeRate = exchangeRateDao.findByCodePair(baseCurrencyCode, targetCurrencyCode);

        if (maybeExchangeRate.isPresent()) {
            return exchangeByDirectRate(maybeExchangeRate.get(), amount);
        }

        Optional<ExchangeRate> maybeReverseRate = exchangeRateDao.findByCodePair(targetCurrencyCode, baseCurrencyCode);

        if (maybeReverseRate.isPresent()) {
            return exchangeByReverseRate(maybeReverseRate.get(), amount);
        }

        List<ExchangeRate> crossExchangeList = exchangeRateDao.findByCrossRate(baseCurrencyCode, targetCurrencyCode);

        if (crossExchangeList.size() == 2) {
            if (crossExchangeList.getFirst().getTargetCurrency().getCode().equalsIgnoreCase(targetCurrencyCode)) {
                crossExchangeList = crossExchangeList.reversed();
            }
            return exchangeByCrossRate(crossExchangeList, amount);
        }

        throw new NonExistsException("no exchange method found for this currency pair");
    }

    private Exchange exchangeByDirectRate(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal convertedAmount = exchangeRate.getRate().multiply(amount);
        return buildExchange(exchangeRate, amount, convertedAmount);
    }

    private Exchange exchangeByReverseRate(ExchangeRate exchangeRate, BigDecimal amount) {
        Currency baseCurrency = exchangeRate.getBaseCurrency();
        exchangeRate.setBaseCurrency(exchangeRate.getTargetCurrency());
        exchangeRate.setTargetCurrency(baseCurrency);

        BigDecimal rate = BigDecimal.ONE.divide(exchangeRate.getRate());
        BigDecimal convertedAmount = rate.multiply(amount);

        exchangeRate.setRate(rate);

        return buildExchange(exchangeRate, amount, convertedAmount);
    }

    private Exchange exchangeByCrossRate(List<ExchangeRate> crossExchangeList, BigDecimal amount) {
        ExchangeRate toBaseExchangeRate = crossExchangeList.getFirst();
        ExchangeRate toTargetExchangeRate = crossExchangeList.getLast();

        BigDecimal rate = toTargetExchangeRate.getRate().divide(toBaseExchangeRate.getRate());
        BigDecimal convertedAmount = rate.multiply(amount);

        toTargetExchangeRate.setRate(rate);
        toTargetExchangeRate.setBaseCurrency(toBaseExchangeRate.getTargetCurrency());

        return buildExchange(toTargetExchangeRate, amount, convertedAmount);
    }

    private Exchange buildExchange(ExchangeRate exchangeRate, BigDecimal amount, BigDecimal convertedAmount) {
        return new Exchange(
              exchangeRate.getBaseCurrency(),
              exchangeRate.getTargetCurrency(),
              exchangeRate.getRate(),
              amount,
              convertedAmount
        );
    }

    public static ExchangeService getInstance() {
        return INSTANCE;
    }
}