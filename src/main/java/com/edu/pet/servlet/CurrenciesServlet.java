package com.edu.pet.servlet;

import com.edu.pet.dto.CreateCurrencyDto;
import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.AlreadyExistsException;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

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
            List<CurrencyDto> currenciesDto = currencyService.findAll();
            objectMapper.writeValue(writer, currenciesDto);
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();
        List<String> missingFields = Arrays.stream(CreateCurrencyDto.class.getDeclaredFields())
                .map(Field::getName)
                .filter(Predicate.not(parameterMap::containsKey))
                .toList();
        PrintWriter writer = resp.getWriter();
        try {
            if (!missingFields.isEmpty()) {
                resp.setStatus(SC_BAD_REQUEST);
                StringJoiner joiner = new StringJoiner(", ");
                missingFields.forEach(joiner::add);
                objectMapper.writeValue(writer, new ErrorBody("field(-s) not found - " + joiner.toString()));
            }

            CurrencyDto currencvDto = currencyService.save(new CreateCurrencyDto(
                    parameterMap.get("code")[0],
                    parameterMap.get("name")[0],
                    parameterMap.get("sign")[0]
            ));
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