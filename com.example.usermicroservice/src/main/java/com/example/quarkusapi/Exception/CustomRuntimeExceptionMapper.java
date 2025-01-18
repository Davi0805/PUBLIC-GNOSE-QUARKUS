package com.example.quarkusapi.Exception;//package Exception;
//
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.ext.ExceptionMapper;
//import jakarta.ws.rs.ext.Provider;
//
//@Provider
//public class CustomRuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
//
//    @Override
//    public Response toResponse(RuntimeException exception) {
//        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity("Custom runtime error: " + exception.getMessage())
//                .build();
//    }
//}