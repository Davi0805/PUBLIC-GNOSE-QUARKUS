package com.example.quarkusapi.Repositories;

import com.example.quarkusapi.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Hibernate;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public User findUserWithCompanies(String username) {
        return find("SELECT u FROM User u LEFT JOIN FETCH u.userCompanies uc LEFT JOIN FETCH uc.company WHERE u.username = ?1", username).firstResult();
    }

//public User findUserWithCompanies(String username) {
//    // Buscar apenas o usuário (sem JOIN FETCH)
//    User user = find("SELECT u FROM User u WHERE u.username = ?1", username).firstResult();
//
//    // Se encontrou o usuário, carregar suas empresas separadamente
//    if (user != null) {
//        Hibernate.initialize(user.getUserCompanies());
//    }
//
//    return user;
//}

}
