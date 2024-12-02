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

    @Column(nullable = false, length = 50)
    public String username;

    @Column(nullable = false, length = 255)
    public String password;

    public Boolean checkHashPassword(String password)
    {
        return BCrypt.checkpw(password, this.password);
    }

}