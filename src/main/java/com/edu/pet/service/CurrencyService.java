package com.edu.pet.service;

import com.edu.pet.dao.CurrencyDao;
import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyService {

    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ModelMapper modelMapper = new ModelMapper();

    private CurrencyService() {

    }

    {
        modelMapper.typeMap(CreateCurrencyDto.class, Currency.class)
                .addMapping(CreateCurrencyDto::getName, Currency::setFullName);
    }

    public List<CurrencyDto> findAll() throws InternalErrorException {
        List<Currency> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(currency -> modelMapper.map(currency, CurrencyDto.class))
                .collect(Collectors.toList());
    }

    public Optional<CurrencyDto> findByCode(String code) throws InternalErrorException {
        Optional<Currency> maybeCurrency = currencyDao.findByCode(code);
        return maybeCurrency.map(currency -> modelMapper.map(currency, CurrencyDto.class));
    }

    public CurrencyDto save(CreateCurrencyDto createCurrencyDto) throws AlreadyExistsException, InternalErrorException {
        Currency createCurrency = modelMapper.map(createCurrencyDto, Currency.class);
        return modelMapper.map(currencyDao.save(createCurrency), CurrencyDto.class);
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }
}
