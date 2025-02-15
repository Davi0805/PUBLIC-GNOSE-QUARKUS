package com.example.quarkusapi.Repositories;

import com.example.quarkusapi.model.UserCompany;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jose4j.jwk.Use;

import java.util.List;

@ApplicationScoped
public class UserCompanyRepository implements PanacheRepository<UserCompany> {

    public List<UserCompany> findUsersByCompanyId(Long companyId) {

        return find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1", companyId).list();

    }

    public List<UserCompany> findUsersByCompanyIdAndPermission(Long companyId, String permission) {
        return find("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = ?1 AND uc.permission = ?2",
                companyId, permission)
                .list();
    }
}
