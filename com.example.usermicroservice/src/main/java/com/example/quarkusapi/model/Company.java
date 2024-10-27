package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "companies")
public class Company extends PanacheEntity implements Serializable {
    
    @Column(name = "name" , nullable = false, unique = true)
    public String company_name;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public Set<UserCompany> userCompanies;
}
