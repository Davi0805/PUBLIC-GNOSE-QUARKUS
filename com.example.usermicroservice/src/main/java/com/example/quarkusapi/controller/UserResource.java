package com.example.quarkusapi.controller;

import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Exception.UnauthorizedException;
import com.example.quarkusapi.model.RedisCompanies;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserLogin;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.AuthService;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import io.vertx.redis.client.Redis;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

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
    RedisService redisService;

    @Inject
    EmailService emailService;

    @Inject
    JwtUtils jwtUtil;
    @Inject
    AuthService authService;

    // TODO: Adicionar rate limit
    @POST
    @Path("/login")
    @Timed(name = "Post-Login", description = "Latencia para executar login", unit = MetricUnits.MILLISECONDS, absolute = true)
    public Response login(User user) {
        if (user == null || user.username == null || user.password == null)
            throw new BadRequestException("Preencha os campos");

        //validar o usuário e senha
        User foundUser = User.find("username", user.username).firstResult();
        if (!foundUser.emailVerified)
            throw new UnauthorizedException("Email nao verificado!");

        if (foundUser != null && foundUser.checkHashPassword(user.password)) {
            String token = jwtUtil.generateToken(user.username);

            if (!(redisService.saveToken(token, foundUser.userCompanies)))
                throw new InternalServerErrorException("Erro ao salvar token no Redis");

            NewCookie securecookie = new NewCookie.Builder("AUTH_TOKEN")
                                                .value(token)
                                                //.path("/")
                                                .httpOnly(false)
                                                .secure(false) // https
                                                .maxAge(7200)
                                                .sameSite(NewCookie.SameSite.NONE)
                                                .build();
            return Response.ok()
                .cookie(securecookie)
                .entity("{\"token\":\"" + token + "\"}")
                .build();
        }

        throw new UnauthorizedException("User ou senha invalidos");
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("AUTH_TOKEN") String token)
    {

        if (token == null || token.isEmpty())
            throw new BadRequestException("Token vazio");

        // REFATORAR PARA MAIS SAFE
        redisService.deleteToken(token);
        NewCookie expiredcookie = new NewCookie.Builder("AUTH_TOKEN")
                                                .value("")
                                                .maxAge(0)
                                                .path("/")
                                                .secure(false)
                                                .httpOnly(true)
                                                .build();
        return Response
                .status(Response.Status.OK)
                .cookie(expiredcookie)
                .entity("Deslogado com sucesso!")
                .build();
    }
    
//    @GET
//    public Response listaUsers(@QueryParam("page") @DefaultValue("1") int page,
//                               @QueryParam("size") @DefaultValue("10") int size,
//                               @CookieParam("AUTH_TOKEN") String token) {
//        if (page < 1 || size < 1)
//            throw new BadRequestException("Page ou size invalidos");
//
//        if (!redisService.validateToken(token))
//            throw new UnauthorizedException("Token invalido");
//
//        List<User> users = User.findAll().page(page - 1, size).list();
//        long totalUsers = User.count();
//
//        return Response.ok()
//                .header("X-Total-Count", totalUsers)
//                .entity(users)
//                .build();
//    }

    @POST
    /* @ProtectedRoute */
    @Transactional
    public Response criarUser(User user)
    {
        User existingUser  = User.find("username", user.username).firstResult();
        if (existingUser  != null)
            throw new ResourceConflictException("Usuario ja existe");

        user.setHashPassword(user.password);
        user.persist();


        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarUser(@PathParam("id") Long id,
                           @CookieParam("AUTH_TOKEN") String token,
                           @HeaderParam("X-Forwarded-For") String ip,
                           @HeaderParam("User-Agent") String userAgent)
    {
        // Db - query
        User user = User.findById(id);
        if (user == null)
            throw new NotFoundException("Usuario nao encontrado!");

        // AUTH
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        if (empresas.stream().noneMatch(empresa ->
                empresa.getId().getUserId() == user.id))
            throw new UnauthorizedException("Nao faz parte dessa empresa ou nao tem permissao!");


        return Response.ok(user).build();
    }

//    @DELETE
//    @Path("/{id}")
//    @Transactional
//    public Response delete_user(@PathParam("id") Long id)
//    {
//        User foundUser = User.findById(id);
//
//        if (foundUser == null)
//            throw new NotFoundException("Nao encontrado");
//
//        foundUser.delete();
//
//        return Response
//            .status(Response.Status.NO_CONTENT)
//            .build();
//    }
}
