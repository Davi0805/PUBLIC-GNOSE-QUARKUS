package com.example.quarkusapi.controller;

import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Exception.UnauthorizedException;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.RedisCompanies;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserLogin;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.AuthService;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.service.UserService;
import com.example.quarkusapi.utils.JwtUtils;
import io.vertx.redis.client.Redis;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import java.util.List;
import com.example.quarkusapi.service.EmailService;

import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.HttpHeaders;  // Para acessar os cabeçalhos HTTP
import jakarta.ws.rs.core.Cookie;      // Para acessar o cookie
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    AuthService authService;

    @Inject
    UserService userService;

    @Inject
    EmailService emailService;

    @Inject
    RedisService redisService;

    // TODO: Adicionar rate limit
    @POST
    @Path("/login")
    @Timed(name = "Post-Login", description = "Latencia para executar login", unit = MetricUnits.MILLISECONDS, absolute = true)
    public Response login(User user)
    {
        String token = userService.login(user);

            return Response.ok()
                .entity("{\"token\":\"" + token + "\"}")
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String token) {

        if (token != null)
            token = token.substring(7);

        userService.logout(token);

        return Response
                .status(Response.Status.OK)
                .entity("Deslogado com sucesso!")
                .build();
    }

    @POST
    @Transactional
    public Response criarUser(User user) {

        userService.criarUser(user);

                // TODO: adicionar teste para email
        // Hita Serverless func para mandar link de verificacao de email
        String token = redisService.saveEmail(user.id); // TODO: Adicionar ao User Resource para limpar
        emailService.sendEmailVerificationAsync(new EmailVerificationRequest(user.email, user.first_name, token))
                .subscribe().with(ignored -> {}, failure -> {});;

        // TODO: Retornar apenas dados pertinentes ao FRONTEND
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarUser(@PathParam("id") Long id,
                           @HeaderParam("Authorization") String token,
                           @HeaderParam("X-Forwarded-For") String ip,
                           @HeaderParam("User-Agent") String userAgent) {
        if (token != null)
            token = token.substring(7);

        User user = userService.getUserById(id);

        // AUTH
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        if (empresas.stream().noneMatch(empresa ->
                empresa.getId().getUserId() == user.id))
            throw new UnauthorizedException("Nao faz parte dessa empresa ou nao tem permissao!");

        return Response.ok(user).build();
    }
}