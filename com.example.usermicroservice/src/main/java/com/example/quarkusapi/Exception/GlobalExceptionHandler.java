package com.example.quarkusapi.Exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace(); // Apenas para debug; remova em produção

        if (exception.getCause() instanceof jakarta.validation.ConstraintViolationException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid input: " + exception.getCause().getMessage())
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("An unexpected error occurred: " + exception.getMessage())
                .build();
    }
}