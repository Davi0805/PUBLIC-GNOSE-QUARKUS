package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users")
public class User extends PanacheEntity implements Serializable {
    
    @Column(nullable = false, unique = true)
    public String username;

    @Column(nullable = false)
    public String password;

    @Column(nullable = false)
    public String email;
    
    @Column(nullable = false)
    public String first_name;

    @Column(nullable = false)
    public String last_name;

    @Column(nullable = false, length = 1)
    public String permission;
}
