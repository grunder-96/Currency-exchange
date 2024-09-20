package com.edu.pet.util.validation;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;

public class ParamsValidator {

    private final List<String> requiredParams;
    private final HttpServletRequest req;

    public ParamsValidator(HttpServletRequest req, List<String> requiredParams) {
        this.req = Objects.requireNonNull(req, "request is null");
        this.requiredParams = Objects.requireNonNull(requiredParams, "required parameters is null");
    }

    public boolean isValid() {
        return getInvalidParams().isEmpty();
    }

    public List<String> getInvalidParams() {
        return requiredParams.stream()
                .filter(param -> req.getParameter(param).isBlank())
                .toList();
    }
}