package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/events")
public class EventsResource {

    // Substituir por logica mais safe talvez com redis e hash
    @POST
    @Transactional
    @Path("/verify-email/{id}")
    public Response verifyEmail(@PathParam("id") Long id) {
        User user = User.findById(id);
        if (user == null || user.emailVerified)
            throw new NotFoundException("Usuario nao encontrado ou ja verificado");

        user.emailVerified = true;
        user.persist();
        return Response.ok().build();
    }


}
