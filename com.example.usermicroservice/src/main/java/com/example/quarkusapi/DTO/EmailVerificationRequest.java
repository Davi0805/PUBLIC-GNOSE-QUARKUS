package com.example.quarkusapi.DTO;

public class EmailVerificationRequest {
    private final String email;
    private final String name;
    private final Long id;

    public EmailVerificationRequest(String email, String name) {
        this.email = email;
        this.name = name;
        this.id = null;}

    public EmailVerificationRequest(String email, String name, Long id) {
        this.email = email;
        this.name = name;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
