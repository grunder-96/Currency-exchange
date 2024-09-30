package com.edu.pet.servlet;

import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.CurrencyService;
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
import java.util.*;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        try {
            resp.setStatus(SC_OK);
            objectMapper.writeValue(writer, currencyService.findAll());
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        ParamsValidator paramsValidator = new ParamsValidator(req, List.of("name", "code", "sign"));

        try {
            if (!paramsValidator.isValid()) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("parameter(-s) not found or empty - %s".formatted(
                        String.join(", ", paramsValidator.getInvalidParams())
                )));
                return;
            }

            String code = req.getParameter("code").trim();

            if (!CurrencyCodeValidator.isValid(code)) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("currency code is invalid"));
                return;
            }

            CreateCurrencyDto createCurrencyDto = new CreateCurrencyDto(
                    code,
                    req.getParameter("name").trim(),
                    req.getParameter("sign").trim()
            );

            CurrencyDto currencvDto = currencyService.save(createCurrencyDto);
            resp.setStatus(SC_CREATED);
            objectMapper.writeValue(writer, currencvDto);
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