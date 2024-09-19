package com.edu.pet.servlet;

import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.CurrencyService;
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
import java.util.stream.Collectors;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        try {
            objectMapper.writeValue(writer, currencyService.findAll());
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
        ParamsValidator paramsValidator = new ParamsValidator(req, List.of("name", "code", "sign"));

        try {
            if (!paramsValidator.isValid()) {
                objectMapper.writeValue(writer, new ErrorBody("parameter(-s) not found or empty values - %s".formatted(
                        paramsValidator.getInvalidParams().stream().collect(Collectors.joining(", "))
                )));
                resp.setStatus(SC_BAD_REQUEST);
                return;
            }

            CreateCurrencyDto createCurrencyDto = new CreateCurrencyDto(
                    req.getParameter("code"),
                    req.getParameter("name"),
                    req.getParameter("sign")
            );

            CurrencyDto currencvDto = currencyService.save(createCurrencyDto);
            objectMapper.writeValue(writer, currencvDto);
            resp.setStatus(SC_CREATED);
        } catch (AlreadyExistsException e) {
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
            resp.setStatus(SC_CONFLICT);
        } catch (InternalErrorException e) {
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
        } finally {
            writer.close();
        }
    }
}