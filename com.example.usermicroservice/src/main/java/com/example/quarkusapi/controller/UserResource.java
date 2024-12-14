package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.UserLogin;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.HttpHeaders;  // Para acessar os cabeçalhos HTTP
import jakarta.ws.rs.core.Cookie;      // Para acessar o cookie

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    RedisService redisService;

    @Inject
    JwtUtils jwtUtil;

    @POST
    @Path("/login")
    public Response login(User user) {
        if (user == null || user.username == null || user.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Usuario e senha nao fornecido").build();
        }
        //validar o usuário e senha
        User foundUser = User.find("username", user.username).firstResult();
        if (foundUser != null && foundUser.checkHashPassword(user.password)) {
            String token = jwtUtil.generateToken(user.username);
            if ((redisService.saveToken(token, foundUser.userCompanies)) == false)
                return Response.status(Response.Status.BAD_REQUEST).entity("Falha ao salvar token no redis").build();
            NewCookie securecookie = new NewCookie.Builder("AUTH_TOKEN")
                                                .value(token)
                                                .path("/")
                                                .httpOnly(true)
                                                .secure(false)
                                                .maxAge(7200)
                                                .sameSite(NewCookie.SameSite.STRICT)
                                                .build();
            return Response.ok()
                .cookie(securecookie)
                .header("Authorization", "Bearer " + token)
                .entity("{\"token\":\"" + token + "\"}")
                .build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@CookieParam("AUTH_TOKEN") String token)
    {

        if (token == null || token.isEmpty())
            return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("Token nao encontrado!")
            .build();

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
    
    @GET
    @ProtectedRoute
    public Response listaUsers(@QueryParam("page") @DefaultValue("1") int page,
                               @QueryParam("size") @DefaultValue("10") int size) {
        if (page < 1 || size < 1) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Pagina ou tamanho muito pequeno").build();
        }

        List<User> users = User.findAll().page(page - 1, size).list();
        long totalUsers = User.count();

        return Response.ok()
                .header("X-Total-Count", totalUsers)
                .entity(users)
                .build();
    }

    @POST
    /* @ProtectedRoute */
    @Transactional
    public Response criarUser(User user)
    {
        User existingUser  = User.find("username", user.username).firstResult();
        if (existingUser  != null) {
            return Response.status(Response.Status.CONFLICT).entity("Usuario ja existe").build();
        }
        user.setHashPassword(user.password);
        user.persist();
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{id}")
    @ProtectedRoute
    public User buscarUser(@PathParam("id") Long id)
    {
        return User.findById(id);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete_user(@PathParam("id") Long id)
    {
        User foundUser = User.findById(id);
        
        if (foundUser == null) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity("User not found!")
                .build();
        }
        
        foundUser.delete();
        
        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }
}
