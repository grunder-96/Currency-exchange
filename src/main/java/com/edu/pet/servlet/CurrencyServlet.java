package com.edu.pet.servlet;

import com.edu.pet.dto.CurrencyDto;
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
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Optional<String> maybeCode = Optional.ofNullable(req.getPathInfo());

        if (maybeCode.isEmpty()) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "currency code missing in url");
            return;
        }

        String code = maybeCode.get().replace("/", "");

        if (!CurrencyCodeValidator.isValid(code)) {
            ResponseWrapper.configureErrorResponse(resp, SC_BAD_REQUEST, "currency code is invalid");
            return;
        }

        try {
            Optional<CurrencyDto> maybeCurrencyDto = currencyService.findByCode(code);

            if (maybeCurrencyDto.isEmpty()) {
                ResponseWrapper.configureErrorResponse(resp, SC_NOT_FOUND, "such currency is not in the database");
                return;
            }

            ResponseWrapper.configureResponse(resp, SC_OK, maybeCurrencyDto.get());
        } catch (InternalErrorException e) {
            ResponseWrapper.configureErrorResponse(resp, SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}