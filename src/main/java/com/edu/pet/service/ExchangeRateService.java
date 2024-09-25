package com.edu.pet.service;

import com.edu.pet.dao.ExchangeRateDao;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ExchangeRate;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {

    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final ModelMapper modelMapper = new ModelMapper();

    private ExchangeRateService() {

    }

    public List<RateDto> findAll() throws InternalErrorException {
        List<ExchangeRate> exchangeRates = exchangeRateDao.findAll();
        return exchangeRates.stream()
                .map(exchangeRate -> modelMapper.map(exchangeRate, RateDto.class))
                .toList();
    }

    public Optional<RateDto> findByCodePair(String baseCurrencyCode, String targetCurrencyCode) throws InternalErrorException {
        Optional<ExchangeRate> maybeExchangeRate = exchangeRateDao.findByCodePair(baseCurrencyCode, targetCurrencyCode);
        return maybeExchangeRate.map(exchangeRate -> modelMapper.map(maybeExchangeRate, RateDto.class));
    }

    public RateDto save(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws InternalErrorException {
        return modelMapper.map(exchangeRateDao.save(baseCurrencyCode, targetCurrencyCode, rate), RateDto.class);
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }
}