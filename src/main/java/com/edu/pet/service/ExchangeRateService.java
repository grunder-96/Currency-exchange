package com.edu.pet.service;

import com.edu.pet.dao.CurrencyDao;
import com.edu.pet.dao.ExchangeRateDao;
import com.edu.pet.dto.ExchangeRateDto;
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

//    {
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        modelMapper.createTypeMap(ExchangeRate.class, ExchangeRateDto.class).addMappings(mapper -> {
//            mapper.skip(ExchangeRateDto::setBaseCurrency);
//            mapper.skip(ExchangeRateDto::setTargetCurrency);
//        }).setPostConverter(toExchangeRateDto());
//    }


    public List<ExchangeRateDto> findAll() {
        List<ExchangeRate> exchangeRates = exchangeRateDao.findAll();
        return exchangeRates.stream()
                .map(exchangeRate -> modelMapper.map(exchangeRate, ExchangeRateDto.class))
                .toList();
    }

    public Optional<ExchangeRateDto> findByCodePair(String baseCurrencyCode, String targetCurrencyCode) {
        Optional<ExchangeRate> maybeExchangeRate = exchangeRateDao.findByCodePair(baseCurrencyCode, targetCurrencyCode);
        return maybeExchangeRate.map(exchangeRate -> modelMapper.map(maybeExchangeRate, ExchangeRateDto.class));
    }

//    private Converter<ExchangeRate, ExchangeRateDto> toExchangeRateDto() {
//        return context -> {
//            ExchangeRate source = context.getSource();
//            ExchangeRateDto destination = context.getDestination();
//
//            destination.setBaseCurrency(modelMapper.map(currencyDao.findById(source.getBaseCurrency()).get(), CurrencyDto.class));
//            destination.setTargetCurrency(modelMapper.map(currencyDao.findById(source.getTargetCurrency()).get(), CurrencyDto.class));
//
//            return context.getDestination();
//        };
//    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }
}