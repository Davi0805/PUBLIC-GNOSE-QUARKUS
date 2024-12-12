package com.example.quarkusapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.io.Serializable;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "user_company")
public class UserCompany extends PanacheEntityBase implements Serializable
{
    
    @EmbeddedId
    public UserCompanyId id = new UserCompanyId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-userCompany")
    public User user;

    @ManyToOne
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    @JsonBackReference
    public Company company;

    @Column(nullable = false, length = 1)
    public String permission;

    public User getUser() {
        return user;
    }    

    public static List<UserCompany> findByCompanyId(Long companyId) {
        return find("company.id", companyId).list();
    }

    public static List<User> findUsersByCompanyId(Long companyId) {
        List<User> users = find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1", companyId)
                .list();
        return users;
    }

    public static List<User> findUsersByCompanyIdAndPermission(Long companyId, String permission) {
        return find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1 AND uc.permission = ?2", 
                    companyId, permission)
                .list();
    }
}
