package com.example.quarkusapi.DTO;

import com.example.quarkusapi.model.RedisCompanies;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class Login2FrontDTO {

    private String token;

    private List<RedisCompanies> companies;

    public Login2FrontDTO() {
    }

    @JsonCreator
    public Login2FrontDTO(@JsonProperty("token") String token, @JsonProperty("companies") List<RedisCompanies> companies) {
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
