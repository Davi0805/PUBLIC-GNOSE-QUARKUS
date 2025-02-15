package com.example.quarkusapi.controller;

import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.User;
import jakarta.transaction.Transactional;
import com.example.quarkusapi.service.RedisService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

@Path("/events")
public class EventsResource {

    @Inject
    RedisService redisService;
    @Inject
    UserRepository userRepository;

    // Substituir por logica mais safe talvez com redis e hash
    @GET
    @Transactional
    @Path("/verify-email/{token}")
    public Response verifyEmail(@PathParam("token") String token) {
        Long id = redisService.verifyEmailToken(token);
        User user = userRepository.findById(id);
        if (user == null || user.emailVerified)
            throw new NotFoundException("Usuario nao encontrado ou ja verificado");

        user.emailVerified = true;
        userRepository.persist(user);
        
        redisService.deleteToken(token);
        return Response.ok().build();
    }


}
