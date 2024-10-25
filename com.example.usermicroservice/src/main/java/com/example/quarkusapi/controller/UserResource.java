package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
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
    public List<User> listaUsers()
    {
        return User.listAll();
    }

    @POST
    @Transactional
    public Response criarUser(User user)
    {
        user.persist();
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/{id}")
    public User buscarUser(@PathParam("id") Long id)
    {
        return User.findById(id);
    }
}
