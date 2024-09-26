package com.edu.pet.service;

import com.edu.pet.dao.CurrencyDao;
import com.edu.pet.dao.ExchangeRateDao;
import com.edu.pet.dto.CreateUpdateRateDto;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.Currency;
import com.edu.pet.model.ExchangeRate;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

public class ExchangeRateService {

    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
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

    public RateDto save(CreateUpdateRateDto dto) throws InternalErrorException, NonExistsException, AlreadyExistsException {
        return modelMapper.map(exchangeRateDao.save(convertToExchangeRate(dto)), RateDto.class);
    }

    public RateDto update(CreateUpdateRateDto dto) throws InternalErrorException, NonExistsException {
        return modelMapper.map(exchangeRateDao.update(convertToExchangeRate(dto)), RateDto.class);
    }

    private ExchangeRate convertToExchangeRate(CreateUpdateRateDto dto) throws NonExistsException {
        Optional<Currency> maybeBaseCurrency = currencyDao.findByCode(dto.getBaseCurrencyCode());
        Optional<Currency> maybeTargetCurrency = currencyDao.findByCode(dto.getTargetCurrencyCode());

        ExchangeRate createExchangeRate;

        if (maybeBaseCurrency.isPresent() && maybeTargetCurrency.isPresent()) {
            createExchangeRate = new ExchangeRate();
            createExchangeRate.setBaseCurrency(maybeBaseCurrency.get());
            createExchangeRate.setTargetCurrency(maybeTargetCurrency.get());
            createExchangeRate.setRate(dto.getRate());
            return createExchangeRate;
        }

        throw new NonExistsException("One (or both) currencies from the currency pair do not exist in the database");
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }
}