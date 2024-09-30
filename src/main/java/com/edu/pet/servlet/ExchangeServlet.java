package com.edu.pet.servlet;

import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.exception.NonExistsException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.ExchangeService;
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
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        ParamsValidator paramsValidator = new ParamsValidator(req, List.of("from", "to", "amount"));
        try {
            if (!paramsValidator.isValid()) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("parameter(-s) not found or empty - %s".formatted(
                        String.join(", ", paramsValidator.getInvalidParams())
                )));
                return;
            }

            String baseCurrencyCode = req.getParameter("from").trim();
            String targetCurrencyCode = req.getParameter("to").trim();

            if (!(
                    CurrencyCodeValidator.isValid(baseCurrencyCode) &&
                    CurrencyCodeValidator.isValid(targetCurrencyCode)
                )) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("one or both currency codes are not valid"));
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(req.getParameter("amount").trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("amount must be greater than zero");
                }
            } catch (RuntimeException e) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer,
                        new ErrorBody(e.getClass().equals(IllegalArgumentException.class) ?
                                e.getMessage() : "amount is not valid"));
                return;
            }

            resp.setStatus(SC_OK);
            objectMapper.writeValue(writer, exchangeService.exchange(baseCurrencyCode, targetCurrencyCode, amount));
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