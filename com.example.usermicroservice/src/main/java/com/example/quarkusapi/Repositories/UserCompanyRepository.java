package com.example.quarkusapi.Repositories;

import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserCompany;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jose4j.jwk.Use;

import java.util.List;

@ApplicationScoped
public class UserCompanyRepository implements PanacheRepository<UserCompany> {

    public List<User> findUsersByCompanyId(Long companyId) {
        return getEntityManager()
                .createQuery("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = :companyId", User.class)
                .setParameter("companyId", companyId)
                .getResultList();
    }

    public List<User> findUsersByCompanyIdAndPermission(Long companyId, String permission) {
        return getEntityManager()
                .createQuery("SELECT uc.user FROM UserCompany uc WHERE uc.company.id = :companyId AND uc.permission = :permission", User.class)
                .setParameter("companyId", companyId)
                .setParameter("permission", permission)
                .getResultList();
    }
}
