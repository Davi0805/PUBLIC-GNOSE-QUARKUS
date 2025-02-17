package com.example.quarkusapi.DTO;

import com.example.quarkusapi.model.RedisCompanies;

import java.util.List;

public class Login2FrontDTO {

    private String token;

    private List<RedisCompanies> companies;

    public Login2FrontDTO() {
    }

    public Login2FrontDTO(String token, List<RedisCompanies> companies) {
        this.token = token;
        this.companies = companies;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<RedisCompanies> getCompanies() {
        return this.companies;
    }

    public void setCompanies(List<RedisCompanies> companies) {
        this.companies = companies;
    }

    public Login2FrontDTO token(String token) {
        this.token = token;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " token='" + getToken() + "'" +
            ", companies='" + getCompanies() + "'" +
            "}";
    }
}
