package com.example.quarkusapi.Exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceConflictExceptionMapper implements ExceptionMapper<ResourceConflictException> {

    @Override
    public Response toResponse(ResourceConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity("Conflito: " + exception.getMessage())
                .build();
    }
}