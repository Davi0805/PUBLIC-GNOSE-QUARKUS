package com.example.quarkusapi.DTO;

public class EmailVerificationRequest {
    private final String email;
    private final String name;
    private final String token;

    public EmailVerificationRequest(String email, String name, String token) {
        this.email = email;
        this.name = name;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }
}
