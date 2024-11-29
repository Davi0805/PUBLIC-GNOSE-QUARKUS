package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-userCompany")
    public Set<UserCompany> userCompanies;

    public Void setHashPassword(String password)
    {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        return null;
    }

    public Boolean checkHashPassword(String password)
    {
        return BCrypt.checkpw(password, this.password);
    }
}
