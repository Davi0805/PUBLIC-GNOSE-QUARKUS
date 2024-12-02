package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
public class User extends PanacheEntity implements Serializable
{
    
    @Column(nullable = false, unique = true, length = 50)
    public String username;

    @Column(nullable = false, length = 255)
    public String password;

    @Column(nullable = false, updatable = false, length = 180)
    public String email;
    
    @Column(nullable = false, length = 50)
    public String first_name;

    @Column(nullable = false, length = 100)
    public String last_name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-userCompany")
    public Set<UserCompany> userCompanies;

    public Void setHashPassword(String password)
    {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        return null;
    }
}
