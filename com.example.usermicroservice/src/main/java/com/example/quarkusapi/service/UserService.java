package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.DTO.Login2FrontDTO;
import com.example.quarkusapi.Exception.*;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.RedisCompanies;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.utils.JwtUtils;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.TimeoutException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.NewCookie;
import org.hibernate.exception.ConstraintViolationException;

import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    @Inject
    JwtUtils jwtUtil;

    @Inject
    RedisService redisService;

    @Inject
    AuthService authService;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public void criarUser(User user) throws ResourceConflictException
    {
        try{
            user.setHashPassword(user.password);
            userRepository.persist(user);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException)
                throw new ResourceConflictException("Usuario ja existe");
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro ao criar usuario");
        } catch (Exception e) {
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro inesperado ao criar usuario");
        }
    }


    public User getUserById(Long id) throws NotFoundException
    {
        User user = userRepository.findById(id);
        if (user == null)
            throw new NotFoundException("Usuario nao encontrado!");

        return user;
    }


    //TODO: LIMPAR CODIGO e Deixar mais seguro
    public Login2FrontDTO login(User user, String ClientIp, String userAgent) throws NotFoundException {

        // Refactorar
        int loginAttempts = authService.BruteForceCheck(ClientIp, userAgent);

        User foundUser = userRepository.findUserWithCompanies(user.username);
        if (foundUser == null)
            throw new BadRequestException("User nao encontrado");

        if (!foundUser.emailVerified)
            throw new UnauthorizedException("Email nao verificado!");

        if (foundUser.checkHashPassword(user.password)) {
            authService.RegisterAuthAttempt(ClientIp, loginAttempts);

            String token = jwtUtil.generateToken(user.username);

            if (!(redisService.saveToken(token, foundUser.userCompanies)))
                throw new InternalServerErrorException("Erro ao salvar token no Redis");

            List<RedisCompanies> companies = foundUser.userCompanies.stream()
                                        .map(uc -> new RedisCompanies(
                                                                new RedisCompanies.Id(uc.id.getUserId(),
                                                                uc.id.getCompanyId()),
                                                                uc.permission,
                                                                uc.company.company_name))
                                                                .toList();
            return new Login2FrontDTO(token, companies);
        }
        return null;
    }


    public Void logout(String token)
    {
        if (token == null || token.length() < 7)
            throw new BadRequestException("Token vazio");

        token = token.substring(7);

        // REFATORAR PARA MAIS SAFE
        redisService.deleteToken(token);

        return null;
    }
}
