package com.example.quarkusapi.Repositories;

import com.example.quarkusapi.model.Company;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {
    public Company findCompanyByName(String name) {
        return find("name", name).firstResult();
    }
}
