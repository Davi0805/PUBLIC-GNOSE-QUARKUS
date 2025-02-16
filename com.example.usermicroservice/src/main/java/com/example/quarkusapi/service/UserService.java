package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.Exception.*;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.utils.JwtUtils;
import io.smallrye.mutiny.TimeoutException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.NewCookie;

import java.time.Duration;

@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    @Inject
    private JwtUtils jwtUtil;

    @Inject
    private RedisService redisService;

    @Inject
    private AuthService authService;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void criarUser(User user) throws ResourceConflictException
    {
        // Checa conflito com username e email
        User existingUser  = userRepository.find("username = ?1 or email = ?2", user.username, user.email).firstResult();
        if (existingUser != null)
            throw new ResourceConflictException(existingUser.username.equals(user.username) ?
                        "Nome de usuario ja existe" : "Email ja existe");


        user.setHashPassword(user.password);


        userRepository.persist(user);

    }

    public User getUserById(Long id) throws NotFoundException
    {
        User user = userRepository.findById(id);
        if (user == null)
            throw new NotFoundException("Usuario nao encontrado!");


        return user;
    }
    //TODO: LIMPAR CODIGO e Deixar mais seguro
    public String login(User user, String ClientIp, String userAgent) throws NotFoundException {

        // Refactorar
        int loginAttempts = authService.BruteForceCheck(ClientIp, userAgent);

//        // OTIMIZACAO? GERA TOKEN ASYNCRONO
//        String token = jwtUtil.generateTokenAsync(user.username).ifNoItem().after(Duration.ofSeconds(10)).fail()
//                .onFailure(TimeoutException.class).recoverWithItem(() -> {
//                    throw new InternalServerErrorException("Token generation timed out");
//                }).await().indefinitely();

        //validar o usuário e senha
        User foundUser = userRepository.find("username", user.username).firstResult();
        if (foundUser == null)
            throw new BadRequestException("User nao encontrado");

        if (!foundUser.emailVerified)
            throw new UnauthorizedException("Email nao verificado!");

        if (foundUser.checkHashPassword(user.password)) {
            authService.RegisterAuthAttempt(ClientIp, loginAttempts);

            String token = jwtUtil.generateToken(user.username);

            if (!(redisService.saveToken(token, foundUser.userCompanies)))
                throw new InternalServerErrorException("Erro ao salvar token no Redis");

//            NewCookie securecookie = new NewCookie.Builder("AUTH_TOKEN")
//                    .value(token)
//                    //.path("/")
//                    .httpOnly(false)
//                    .secure(false) // https
//                    .maxAge(7200)
//                    .sameSite(NewCookie.SameSite.NONE)
//                    .build();

            return token;
        }
        return null;
    }

    public Void logout(String token)
    {
        if (token != null && token.length() > 7)
            token = token.substring(7);
        else
            throw new BadRequestException("Token vazio");

        // REFATORAR PARA MAIS SAFE
        redisService.deleteToken(token);

        return null;
    }
}
