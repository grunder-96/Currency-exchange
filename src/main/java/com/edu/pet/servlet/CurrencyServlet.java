package com.edu.pet.servlet;

import com.edu.pet.exception.InternalErrorException;
import com.edu.pet.model.ErrorBody;
import com.edu.pet.service.CurrencyService;
import com.edu.pet.util.validation.CodeValidator;
import com.edu.pet.util.validation.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final Validator<String> validator = new CodeValidator();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        Optional<String> maybeCode = Optional.ofNullable(req.getPathInfo());
        String code;
        try {
            if (maybeCode.isEmpty() || !validator.isValid(code = maybeCode.get().replace("/", ""))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(writer, new ErrorBody("currency code missing in url or is not valid"));
                return;
            }
            if (currencyService.findByCode(code).isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(writer, new ErrorBody("such currency is not in the database"));
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(writer, currencyService.findByCode(code).get());
        } catch (InternalErrorException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(writer, new ErrorBody(e.getMessage()));
        } finally {
            writer.close();
        }
    }
}