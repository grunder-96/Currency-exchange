package com.edu.pet.servlet;

import com.edu.pet.dto.CreateUpdateRateDto;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.ExchangeRateService;
import com.edu.pet.util.validation.CurrencyCodeValidator;
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
        PrintWriter writer = resp.getWriter();
        Optional<String> maybeCurrencyPair = Optional.ofNullable(req.getPathInfo());

        try {
            if (maybeCurrencyPair.isEmpty()) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("currency pair missing in url"));
                return;
            }

            String currencyPair = maybeCurrencyPair.get().replace("/", "");
            String baseCurrencyCode;
            String targetCurrencyCode;

            if (!(currencyPair.matches("[a-zA-Z]{6}") &&
                  CurrencyCodeValidator.isValid(baseCurrencyCode = currencyPair.substring(0, 3)) &&
                  CurrencyCodeValidator.isValid(targetCurrencyCode = currencyPair.substring(3)))) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("one or both currency codes are not valid"));
                return;
            }

            Optional<RateDto> maybeExchangeRateDto = exchangeRateService.findByCodePair(baseCurrencyCode, targetCurrencyCode);

            if (maybeExchangeRateDto.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(writer, new ErrorBody("exchange rate for the pair not found"));
                return;
            }

            resp.setStatus(SC_OK);
            objectMapper.writeValue(writer, maybeExchangeRateDto.get());
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        Optional<String> maybeCurrencyPair = Optional.ofNullable(req.getPathInfo());

        try {
            if (maybeCurrencyPair.isEmpty()) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("currency pair missing in url"));
                return;
            }

            String currencyPair = maybeCurrencyPair.get().replace("/", "");
            String baseCurrencyCode;
            String targetCurrencyCode;

            if (!(currencyPair.matches("[a-zA-Z]{6}") &&
                  CurrencyCodeValidator.isValid(baseCurrencyCode = currencyPair.substring(0, 3)) &&
                  CurrencyCodeValidator.isValid(targetCurrencyCode = currencyPair.substring(3)))) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("one or both currency codes are not valid"));
                return;
            }

            Optional<String> maybeRate = Optional.ofNullable(req.getReader().readLine());

            if (maybeRate.isEmpty() || !maybeRate.get().contains("rate=")) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("rate missing or empty"));
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
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer,
                        new ErrorBody(e.getClass().equals(IllegalArgumentException.class) ?
                                e.getMessage() : "rate is not valid"));
                return;
            }

            RateDto updatedRateDto = exchangeRateService.update(new CreateUpdateRateDto(baseCurrencyCode, targetCurrencyCode, rate));

            resp.setStatus(SC_OK);
            objectMapper.writeValue(writer, updatedRateDto);
        } catch (NonExistsException e) {
            resp.setStatus(SC_NOT_FOUND);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();

        }
    }
}