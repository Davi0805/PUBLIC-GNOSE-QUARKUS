package com.example.quarkusapi.model;

import java.io.Serializable;

import jakarta.validation.constraints.*;

import org.mindrot.jbcrypt.BCrypt;

public class newEmployee implements Serializable
{
    public newEmployee()
    {

    }

    public newEmployee(String username, String password, String email,
                    String first_name, String last_name, Long company_id,
                    String company_permission)
    {
        this.username = username;
        this.password = password;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.company_id = company_id;
        this.company_permission = company_permission;
    }

    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 50)
    private String username;
    
    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 255)
    private String password;

    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 180)
    private String email;

    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 50)
    private String first_name;

    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 100)
    private String last_name;

    private Long company_id;

    @NotEmpty(message = "Preencha todos os campos!")
    @Size(max = 1, message = "Permissao invalida!")
    private String company_permission;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }

    public Long getCompany_id() { return company_id; }
    public void setCompany_id(Long company_id) { this.company_id = company_id; }

    public String getCompany_permission() { return company_permission; }
    public void setCompany_permission(String company_permission) { this.company_permission = company_permission; }

    public Void setHashPassword(String password)
    {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        return null;
    }

}

/* 
! Validation Annotation Cheatsheet:

@NotNull: Cannot be null
@NotEmpty: Cannot be null or empty
@NotBlank: Cannot be null, trimmed length > 0
@Size: Control string/collection length
@Min / @Max: Number range
@Positive / @Negative: Number sign
@Email: Email format
@Pattern: Regex validation
@DecimalMin / @DecimalMax: Decimal range
@Past / @Future: Date validation
@AssertTrue / @AssertFalse: Boolean conditions
 */