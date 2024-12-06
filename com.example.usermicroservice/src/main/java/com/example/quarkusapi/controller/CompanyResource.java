package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("/company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource
{
    @Inject 
    RedisService redisService;

    @POST
    @Transactional
    public Response criar_empresa(Company req)
    {
        if (req == null || req.company_name == null) // Adicionar mais checagems apos deixar entity mais cheia
        {
            return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("Preencha devidamente todos os campos")
            .build();
        }
        req.persist();
        return Response
        .status(Response.Status.OK)
        .entity(req)
        .build();
    }

    @POST
    @Path("/add_func")
    @Transactional
    public Response add_employee(UserCompany req)
    {
        if (req == null)
        {
            return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("Preencha todos os campos!")
            .build();
        }
        User user = User.findById(req.id.getUserId());
        Company company = Company.findById(req.id.getCompanyId());
        
        if (user == null || company == null) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Usuário ou empresa não encontrados!")
                .build();
        }
        
        // Validate permission field
        if (req.permission == null || req.permission.isEmpty()) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Permissão inválida!")
                .build();
        }
        
        // Set relationships explicitly
        req.user = user;
        req.company = company;
        
        req.persist();
        return Response
            .status(Response.Status.OK)
            .entity(req)
            .build();
    }

}