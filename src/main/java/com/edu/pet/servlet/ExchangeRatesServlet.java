package com.edu.pet.servlet;

import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

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
}