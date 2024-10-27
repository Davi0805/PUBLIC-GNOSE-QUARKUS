package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
            return Response.status(Response.Status.BAD_REQUEST).entity("Username and password must be provided").build();
        }
        // Aqui você deve validar o usuário e senha
        User foundUser = User.find("username", user.username).firstResult();
        if (foundUser != null && foundUser.password.equals(user.password)) {
            String token = jwtUtil.generateToken(user.username);
            redisService.saveToken(user.username, token);
            return Response.ok()
                .header("Authorization", "Bearer " + token)
                .entity("{\"token\":\"" + token + "\"}")
                .build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    @GET
    @ProtectedRoute
    public Response listaUsers(@QueryParam("page") @DefaultValue("1") int page,
                               @QueryParam("size") @DefaultValue("10") int size) {
        if (page < 1 || size < 1) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Page and size must be greater than 0").build();
        }

        List<User> users = User.findAll().page(page - 1, size).list();
        long totalUsers = User.count();

        return Response.ok()
                .header("X-Total-Count", totalUsers)
                .entity(users)
                .build();
    }

    @POST
    @ProtectedRoute
    @Transactional
    public Response criarUser(User user)
    {
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
}
