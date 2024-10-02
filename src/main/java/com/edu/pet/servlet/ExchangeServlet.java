package com.edu.pet.servlet;

import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.service.ExchangeService;
import com.edu.pet.util.ResponseWrapper;
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
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final ExchangeService exchangeService = ExchangeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EmptyParamsValidator emptyParamsValidator = new EmptyParamsValidator(req, List.of("from", "to", "amount"));
        if (!emptyParamsValidator.isValid()) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, emptyParamsValidator.getInvalidParamsAsString());
            return;
        }

        String baseCurrencyCode = req.getParameter("from").trim();
        String targetCurrencyCode = req.getParameter("to").trim();

        if (!CurrencyPairValidator.isValid(baseCurrencyCode, targetCurrencyCode)) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "one or both currency codes are not valid");
            return;
        }

            BigDecimal amount;
            try {
                amount = new BigDecimal(req.getParameter("amount").trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("amount must be greater than zero");
                }
            } catch (RuntimeException e) {
                ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, e.getClass().equals(IllegalArgumentException.class) ?
                        e.getMessage() : "amount is not valid");
                return;
            }

        try {
            ResponseWrapper.configureResponse(resp, SC_OK, exchangeService.exchange(baseCurrencyCode, targetCurrencyCode, amount));
        } catch (NonExistsException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_NOT_FOUND, e);
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}