package com.edu.pet.service;

import com.edu.pet.dao.CurrencyDao;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.Currency;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {

    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ModelMapper modelMapper = new ModelMapper();

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
}