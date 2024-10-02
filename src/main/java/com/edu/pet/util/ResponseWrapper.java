package com.edu.pet.util;

import com.edu.pet.model.ErrorBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class ResponseWrapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static void configureSimpleResponse(HttpServletResponse response, int statusCode) {
        response.setStatus(statusCode);
    }

    public static void configureResponse(HttpServletResponse response, int statusCode, Object body) throws IOException {
        configureSimpleResponse(response, statusCode);
        MAPPER.writeValue(response.getWriter(), body);
    }

    public static void configureErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        configureSimpleResponse(response, statusCode);
        MAPPER.writeValue(response.getWriter(), new ErrorBody(message));
    }

    public static void configureErrorResponse(HttpServletResponse response, int statusCode, Exception exception) throws IOException {
        configureErrorResponse(response, statusCode, exception.getMessage());
    }

    private ResponseWrapper() {

    }
}