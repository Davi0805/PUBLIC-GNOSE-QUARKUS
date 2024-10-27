package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "user_company")
public class UserCompany extends PanacheEntityBase implements Serializable {
    
    @EmbeddedId
    public UserCompanyId id = new UserCompanyId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonBackReference
    public User user;

    @ManyToOne
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    @JsonBackReference
    public Company company;

    @Column(nullable = false, length = 1)
    public String permission;
}
