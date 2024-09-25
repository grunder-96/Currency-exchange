package com.edu.pet.servlet;

import com.edu.pet.dto.CreateUpdateRateDto;
import com.edu.pet.dto.RateDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.ExchangeRateService;
import com.edu.pet.util.validation.CurrencyCodeValidator;
import com.edu.pet.util.validation.ParamsValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;
import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        try {
            objectMapper.writeValue(writer,exchangeRateService.findAll());
            resp.setStatus(SC_OK);
        } catch (InternalErrorException e) {
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
        } finally {
            writer.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        ParamsValidator paramsValidator = new ParamsValidator(req, List.of("baseCurrencyCode", "targetCurrencyCode", "rate"));

        try {
            if (!paramsValidator.isValid()) {
                System.out.println("ass");
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("parameter(-s) not found or empty - %s".formatted(
                        String.join(", ", paramsValidator.getInvalidParams())
                )));
                return;
            }

            String baseCurrencyCode = req.getParameter("baseCurrencyCode").trim();
            String targetCurrencyCode = req.getParameter("targetCurrencyCode").trim();

            if (!(CurrencyCodeValidator.isValid(baseCurrencyCode)
                  || CurrencyCodeValidator.isValid(targetCurrencyCode))) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("one or both currency codes are not valid"));
                return;
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(req.getParameter("rate").trim());
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

            RateDto rateDto = exchangeRateService.save(new CreateUpdateRateDto(baseCurrencyCode, targetCurrencyCode, rate));
            resp.setStatus(SC_CREATED);
            objectMapper.writeValue(writer, rateDto);
        } catch (NonExistsException e) {
            resp.setStatus(SC_NOT_FOUND);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } catch (AlreadyExistsException e) {
            resp.setStatus(SC_CONFLICT);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }
}