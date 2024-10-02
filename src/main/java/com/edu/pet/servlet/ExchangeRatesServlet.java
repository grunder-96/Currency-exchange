package com.edu.pet.servlet;

import com.edu.pet.dto.CreateUpdateRateDto;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.service.ExchangeRateService;
import com.edu.pet.util.ResponseWrapper;
import com.edu.pet.util.parsing.DecimalParamParser;
import com.edu.pet.util.validation.CurrencyPairValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;
import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            ResponseWrapper.configureResponse(resp, SC_OK, exchangeRateService.findAll());
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        EmptyParamsValidator emptyParamsValidator = new EmptyParamsValidator(req, List.of("baseCurrencyCode", "targetCurrencyCode", "rate"));

            if (!emptyParamsValidator.isValid()) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST,
                        "parameter(-s) not found or empty - %s".formatted(emptyParamsValidator.getInvalidParamsAsString()));
                return;
            }

            String baseCurrencyCode = req.getParameter("baseCurrencyCode").trim();
            String targetCurrencyCode = req.getParameter("targetCurrencyCode").trim();

            if (!CurrencyPairValidator.isValid(baseCurrencyCode, targetCurrencyCode)) {
                ResponseWrapper.configureErrorResponse(resp,SC_BAD_REQUEST, CurrencyPairValidator.isCurrenciesSame(baseCurrencyCode, targetCurrencyCode) ?
                    "base and target currencies are the same" : "one or both currency codes are not valid");
                return;
            }

            BigDecimal rate;
            try {
                rate = DecimalParamParser.parse(req.getParameter("rate"));
            } catch (RuntimeException e) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, e.getMessage().formatted("rate"));
                return;
            }

        try {
            RateDto rateDto = exchangeRateService.save(new CreateUpdateRateDto(baseCurrencyCode, targetCurrencyCode, rate));
            ResponseWrapper.configureResponse(resp, SC_CREATED, rateDto);
        } catch (NonExistsException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_NOT_FOUND, e);
        } catch (AlreadyExistsException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_CONFLICT, e);
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}