package com.edu.pet.servlet;

import com.edu.pet.dto.CreateUpdateRateDto;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.ExchangeRateService;
import com.edu.pet.util.ResponseWrapper;
import com.edu.pet.util.validation.CurrencyPairValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")){
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Optional<String> maybeCurrencyPair = Optional.ofNullable(req.getPathInfo());

        if (maybeCurrencyPair.isEmpty()) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "currency pair missing in url");
            return;
        }

        String currencyPair = maybeCurrencyPair.get().replace("/", "");

        if (!CurrencyPairValidator.isValid(currencyPair)) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, CurrencyPairValidator.isCurrenciesSame(currencyPair) ?
                    "base and target currencies are the same" : "one or both currency codes are not valid");
            return;
        }

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        try {
            Optional<RateDto> maybeExchangeRateDto = exchangeRateService.findByCodePair(baseCurrencyCode, targetCurrencyCode);

            if (maybeExchangeRateDto.isEmpty()) {
                ResponseWrapper.configureErrorResponse(resp, SC_NOT_FOUND, "exchange rate for the pair not found");
                return;
            }

            ResponseWrapper.configureResponse(resp, SC_OK, maybeExchangeRateDto.get());
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> maybeCurrencyPair = Optional.ofNullable(req.getPathInfo());

            if (maybeCurrencyPair.isEmpty()) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "currency pair missing in url");
                return;
            }

            String currencyPair = maybeCurrencyPair.get().replace("/", "");

            if (!CurrencyPairValidator.isValid(currencyPair)) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST,
                        CurrencyPairValidator.isCurrenciesSame(currencyPair) ?
                                "base and target currencies are the same" : "one or both currency codes are not valid");
                return;
            }

            String baseCurrencyCode = currencyPair.substring(0, 3);
            String targetCurrencyCode = currencyPair.substring(3);

            Optional<String> maybeRate = Optional.ofNullable(req.getReader().readLine());

            if (maybeRate.isEmpty() || !maybeRate.get().contains("rate=")) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "rate missing or empty");
                return;
            }

            String rateValue = maybeRate.get().replace("rate=", "");
            BigDecimal rate;

            try {
                rate = new BigDecimal(rateValue);
                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("rate must be greater than zero");
                }
            } catch (RuntimeException e) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, e.getClass().equals(IllegalArgumentException.class) ?
                        e.getMessage() : "rate is not valid");
                return;
            }

        try {
            RateDto updatedRateDto = exchangeRateService.update(new CreateUpdateRateDto(baseCurrencyCode, targetCurrencyCode, rate));
            ResponseWrapper.configureResponse(resp, SC_OK, updatedRateDto);
        } catch (NonExistsException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_NOT_FOUND, e);
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}