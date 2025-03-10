package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonManagedReference;

// ! ADICIONAR BOOL TERMOS E CONDICOES E BOOL PENDING (JA LIGADO A UMA EMPRESA)

@Entity
@Table(name = "users")
public class User implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    public String username;

    @Column(nullable = false, length = 255)
    public String password;

    @Column(nullable = false, unique = true, updatable = false, length = 180)
    public String email;
    
    @Column(nullable = false, length = 50)
    public String first_name;

    @Column(nullable = false, length = 100)
    public String last_name;

    // TODO: MODIFICAR ANTES DA PROD
    public Boolean emailVerified = false;

    public Boolean termos_e_condicoes = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-userCompany")
    public Set<UserCompany> userCompanies;

    public Void setHashPassword(String password)
    {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        return null;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                '}';
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public boolean checkHashPassword(String passwordToCheck) {
        return BCrypt.checkpw(passwordToCheck, this.password);
    }

    public User fill_User(newEmployee req)
    {
        this.username = req.getUsername();
        setHashPassword(req.getPassword());
        this.email = req.getEmail();
        this.first_name = req.getFirst_name();
        this.last_name = req.getLast_name();
        this.termos_e_condicoes = req.getTermos_e_condicoes();
        
        return this;
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }
}
