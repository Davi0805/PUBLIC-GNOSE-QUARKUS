package com.example.quarkusapi.controller;

import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.model.newEmployee;
import com.example.quarkusapi.filter.ProtectedRoute;
import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.quarkusapi.model.RedisCompanies;
import org.jboss.logging.Logger;

import jakarta.validation.Valid;


@Path("/company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource
{
    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

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
        Company existing_company = Company.find("company_name", req.company_name).firstResult();
        if (existing_company != null)
        {
            existing_company = null; // ! Equivalente de delete em java
            return Response.status(Response.Status.OK)
                            .entity("Empresa ja existe")
                            .build();
        }
        req.persist();
        return Response
        .status(Response.Status.OK)
        .entity(req)
        .build();
    }


    // ? PARA CRIAR USER E ADICIONAR DIRETO EM UMA EMPRESA
    @POST
    @Path("/criar_func")
    @Transactional
    public Response criar_employee(@Valid newEmployee req,
                                    @CookieParam("AUTH_TOKEN") String token)
    {
        if (req == null)
        {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Preencha todos os campos!")
                    .build();
        }
        List<RedisCompanies> empresas = redisService.get_user_companies(token);
        if (empresas.isEmpty() || !empresas.stream()
                                    .anyMatch(empresa -> empresa.getId().getCompanyId() == req.getCompany_id() && empresa.getPermission().equals("A")))
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("Nao tem permissao para acessar esta empresa")
                    .build();
        User existingUser = User.find("username", req.getUsername()).firstResult();
        Company existingCompany = Company.find("id", req.getCompany_id()).firstResult();
        if (existingUser != null)
        {    
            existingUser = null; // ! Equivalente de delete em java
            return Response
            .status(Response.Status.CONFLICT)
            .entity("Usuario ja existe!")
            .build();
        }
        if (existingCompany == null)
            return Response
            .status(Response.Status.CONFLICT)
            .entity("Empresa nao existe!").
            build();
        
        User user_transc = new User().fill_User(req);
        user_transc.persist();

        UserCompany relation = new UserCompany();
        relation.user = user_transc;
        relation.company = existingCompany;
        relation.permission = req.getCompany_permission();

        relation.persist();

        return Response
                    .status(Response.Status.OK)
                    .entity("Usuario criado com sucesso!")
                    .build();
    }

    // TODO: CHECAR SE O CLIENTE ESTA PESQUISANDO OS FUNCS DE UMA EMPRESA QUE ELE FAZ PARTE
    // TODO: E SE TEM PERMISSAO PARA TAL ACAO
    @GET
    @Path("/list_funcs")
    public Response list_company_funcs(@QueryParam("page") @DefaultValue("1") int page,
                                        @QueryParam("size") @DefaultValue("10") int size,
                                        @QueryParam("company") long company_id,
                                        @CookieParam("AUTH_TOKEN") String token)
    {
        List<RedisCompanies> empresas = redisService.get_user_companies(token);
        boolean credentials = empresas.stream()
                                      .anyMatch(empresa -> empresa.getId().getCompanyId() == company_id);
        if (!credentials)
            return Response.status(Response.Status.FORBIDDEN).entity("Nao tem permissao para acessar esta empresa").build();

        if (empresas.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).entity("Nao faz parte de empresa").build();
        LOG.infof("Endpoint %s\n", empresas);
        List<User> req = UserCompany.findUsersByCompanyId(company_id);
        if (req == null || req.isEmpty())
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Empresa ou funcionarios nao existem!")
                    .build();
        return Response
                .status(Response.Status.OK)
                .entity(req)
                .build();
    }


    // ! CONSERTAR AUTENTICACAO E PERMISSAO
    @POST
    @Path("/add_func")
    @Transactional
    public Response add_employee(@CookieParam("AUTH_TOKEN") String token, UserCompany req)
    {
        if (req == null)
        {
            return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("Preencha todos os campos!")
            .build();
        }
        if (redisService.validateToken(token))
        {
            List<RedisCompanies> empresas = redisService.get_user_companies(token);
            boolean credentials = empresas.stream()
                                            .anyMatch(empresa -> empresa.getId().getCompanyId() == req.id.getCompanyId()
                                                                && empresa.getPermission().equals("A"));
            if (!credentials)
                return Response.status(Response.Status.FORBIDDEN).entity("Nao tem permissao para adicionar funcionarios").build();
        }
        User user = User.findById(req.id.getUserId());
        Company company = Company.findById(req.id.getCompanyId());
        if (user == null || company == null) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Usuário ou empresa não encontrados!")
                .build();
        }
        
        if (req.permission == null || req.permission.isEmpty()) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Permissão inválida!")
                .build();
        }
        
        req.user = user;
        req.company = company;
        
        req.persist();
        return Response
            .status(Response.Status.OK)
            .entity(req)
            .build();
    }
}