package com.example.quarkusapi.Exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InternalServerErrorMapper implements ExceptionMapper<InternalServerErrorException> {

    @Override
    public Response toResponse(InternalServerErrorException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Forbidden: " + exception.getMessage())
                .build();
    }
}