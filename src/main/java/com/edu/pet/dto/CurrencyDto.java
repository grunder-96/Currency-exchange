package com.edu.pet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "code", "sign"})
public class CurrencyDto {

    private int id;
    private String code;
    @JsonProperty("name")
    private String fullName;
    private String sign;

    public CurrencyDto(int id, String code, String fullName, String sign) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }

    public CurrencyDto() {

    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSign() {
        return sign;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
