package com.edu.pet.servlet;

import com.edu.pet.dto.CurrencyDto;
import com.edu.pet.exception.CurrencyCodeInvalidException;
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
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        Optional<String> maybeCode = Optional.ofNullable(req.getPathInfo());
        try {
            if (maybeCode.isEmpty()) {
                resp.setStatus(SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("currency code missing in url"));
                return;
            }

            Optional<CurrencyDto> maybeCurrencyDto = currencyService.findByCode(maybeCode.get());

            if (maybeCurrencyDto.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(writer, new ErrorBody("such currency is not in the database"));
                return;
            }

            resp.setStatus(SC_OK);
            objectMapper.writeValue(writer, maybeCurrencyDto.get());
        } catch (CurrencyCodeInvalidException e) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } catch (InternalErrorException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }
}