package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Exception.UnauthorizedException;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.NewCookie;

@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    @Inject
    private JwtUtils jwtUtil;

    @Inject
    private RedisService redisService;

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


//        // TODO: adicionar teste para email
//        // Hita Serverless func para mandar link de verificacao de email
//        String token = redisService.saveEmail(user.id);
//        emailService.sendEmailVerificationAsync(new EmailVerificationRequest(user.email, user.first_name, token))
//                .subscribe().with(ignored -> {}, failure -> {});;
    }

    public User getUserById(Long id) throws NotFoundException
    {
        User user = userRepository.findById(id);
        if (user == null)
            throw new NotFoundException("Usuario nao encontrado!");


        return user;
    }
    //TODO: LIMPAR CODIGO e Deixar mais seguro
    public String login(User user) throws NotFoundException {
        //validar o usuário e senha
        User foundUser = userRepository.find("username", user.username).firstResult();
        if (foundUser == null)
            throw new BadRequestException("User nao encontrado");

        if (!foundUser.emailVerified)
            throw new UnauthorizedException("Email nao verificado!");

        if (foundUser.checkHashPassword(user.password)) {
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
