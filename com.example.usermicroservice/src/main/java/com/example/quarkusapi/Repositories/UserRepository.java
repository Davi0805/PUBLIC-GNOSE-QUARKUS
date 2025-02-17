package com.example.quarkusapi.Repositories;

import com.example.quarkusapi.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findUserWithCompanies(String username) {
        return find("SELECT u FROM User u LEFT JOIN FETCH u.userCompanies uc LEFT JOIN FETCH uc.company WHERE u.username = ?1", username).firstResult();
    }
}
