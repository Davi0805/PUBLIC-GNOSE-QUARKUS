package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name = "users")
public class UserLogin extends PanacheEntity implements Serializable
{

    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String password;

    public Boolean checkHashPassword(String password)
    {
        return BCrypt.checkpw(password, this.password);
    }

}