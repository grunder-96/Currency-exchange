package com.edu.pet.service;

import com.edu.pet.dao.CurrencyDao;
import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.CurrencyCodeInvalidException;
import com.edu.pet.exception.CurrencyCodeUniqueException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import com.edu.pet.util.validation.CurrencyCodeValidator;
import com.edu.pet.util.validation.Validator;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyService {

    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ModelMapper modelMapper = new ModelMapper();
    private final Validator<String> validator = new CurrencyCodeValidator();

    private CurrencyService() {

    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public List<CurrencyDto> findAll() throws InternalErrorException {
        List<Currency> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(currency -> modelMapper.map(currency, CurrencyDto.class))
                .collect(Collectors.toList());
    }

    public Optional<CurrencyDto> findByCode(String code) throws CurrencyCodeInvalidException, InternalErrorException {
        code = code.replace("/", "");
        if (!validator.isValid(code)) {
            throw new CurrencyCodeInvalidException("currency code is invalid");
        }
        Optional<Currency> maybeCurrency = currencyDao.findByCode(code);
        return maybeCurrency.map(currency -> modelMapper.map(currency, CurrencyDto.class));
    }

    public CurrencyDto save(CreateCurrencyDto createCurrencyDto) throws CurrencyCodeUniqueException, InternalErrorException {
        modelMapper.typeMap(CreateCurrencyDto.class, Currency.class).addMapping(CreateCurrencyDto::getName, Currency::setFullName);
        Currency createCurrency = modelMapper.map(createCurrencyDto, Currency.class);
        return modelMapper.map(currencyDao.save(createCurrency), CurrencyDto.class);
    }
}