package com.edu.pet.servlet;

import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.util.ResponseWrapper;
import com.edu.pet.service.CurrencyService;
import com.edu.pet.util.validation.CurrencyCodeValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            ResponseWrapper.configureResponse(resp, SC_OK, currencyService.findAll());
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        EmptyParamsValidator emptyParamsValidator = new EmptyParamsValidator(req, List.of("name", "code", "sign"));

        if (!emptyParamsValidator.isValid()) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST,
                    "parameter(-s) not found or empty - %s".formatted(emptyParamsValidator.getInvalidParamsAsString()));
            return;
        }

        String code = req.getParameter("code").trim();

        if (!CurrencyCodeValidator.isValid(code)) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "currency code is invalid");
            return;
        }

        CreateCurrencyDto createCurrencyDto = new CreateCurrencyDto(
                code,
                req.getParameter("name").trim(),
                req.getParameter("sign").trim()
        );

        try {
            CurrencyDto currencyDto = currencyService.save(createCurrencyDto);
            ResponseWrapper.configureResponse(resp, SC_CREATED, currencyDto);
        } catch (AlreadyExistsException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_CONFLICT, e);
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}