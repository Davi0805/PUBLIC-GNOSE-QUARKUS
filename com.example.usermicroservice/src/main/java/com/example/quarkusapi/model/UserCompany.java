package com.example.quarkusapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import java.io.Serializable;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "user_company")
@RegisterForReflection
public class UserCompany implements Serializable
{
    private Long userId;
    private Long companyId;

    public UserCompany() {}

    @JsonCreator
    public UserCompany(@JsonProperty("user_id") Long user_id, @JsonProperty("companyId") Long company_Id) {
        this.userId = user_id;
        this.companyId = company_Id;
    }

    @EmbeddedId
    public UserCompanyId id;

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

//    public User getUser() {
//        return user;
//    }

//    public static List<UserCompany> findByCompanyId(Long companyId) {
//        return find("company.id", companyId).list();
//    }
//
//    public static List<PanacheEntityBase> findUsersByCompanyId(Long companyId) {
//        List<PanacheEntityBase> users = find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1", companyId)
//                .list();
//        return users;
//    }
//
//    public static List<PanacheEntityBase> findUsersByCompanyIdAndPermission(Long companyId, String permission) {
//        return find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1 AND uc.permission = ?2",
//                    companyId, permission)
//                .list();
//    }

}
