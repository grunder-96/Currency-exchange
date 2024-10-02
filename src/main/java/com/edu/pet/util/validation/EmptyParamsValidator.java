package com.edu.pet.util.validation;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;

public class EmptyParamsValidator {

    private final List<String> requiredParams;
    private final HttpServletRequest req;
    private List<String> invalidParams;

    public EmptyParamsValidator(HttpServletRequest req, List<String> requiredParams) {
        this.req = Objects.requireNonNull(req, "request is null");
        this.requiredParams = Objects.requireNonNull(requiredParams, "required parameters is null");
    }

    public boolean isValid() {
        return getInvalidParams().isEmpty();
    }

    public List<String> getInvalidParams() {
        if (Objects.isNull(invalidParams)) {
            return invalidParams = requiredParams.stream()
                .filter(param -> Objects.isNull(req.getParameter(param)) || req.getParameter(param).isBlank())
                .toList();
        }
        return invalidParams;
    }

    public String getInvalidParamsAsString() {
        return String.join(", ", getInvalidParams());
    }
}