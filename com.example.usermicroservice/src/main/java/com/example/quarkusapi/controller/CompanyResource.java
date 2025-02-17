package com.example.quarkusapi.controller;

import com.example.quarkusapi.DTO.AtribEmpresaDTO;
import com.example.quarkusapi.DTO.CreateUserAdminRequestDTO;
import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.Exception.BadRequestException;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Exception.UnauthorizedException;
import com.example.quarkusapi.Repositories.CompanyRepository;
import com.example.quarkusapi.Repositories.UserCompanyRepository;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.*;
import com.example.quarkusapi.service.AuthService;
import com.example.quarkusapi.service.CompanyService;
import com.example.quarkusapi.service.EmailService;
import com.example.quarkusapi.service.RedisService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.redis.client.Redis;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

import jakarta.validation.Valid;


@Path("/company")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompanyResource
{
    private static final Logger LOG = Logger.getLogger(CompanyResource.class);

    @Inject
    EmailService emailService;
    @Inject
    AuthService authService;
    @Inject
    CompanyService companyService;

    // TODO: ADIONAR FIELD DE TOKEN E CHECAGEM NO REDIS PARA CONVITE
    @POST
    @Path("/create_user_admin")
    @Transactional
    public Response createUserAndAssignAdmin(@Valid CreateUserAdminRequestDTO request) {

        User user = companyService.CreateUserAndEmpresa(request);

        emailService.sendEmailVerificationAsync(new EmailVerificationRequest(user.email, user.first_name, user.id))
                .subscribe().with(ignored -> {}, failure -> {});;

        return Response
                .status(Response.Status.CREATED)
                .entity("Usuario e empresa criada!")
                .build();
    }

    @POST
    @Transactional
    public Response criar_empresa(@HeaderParam("User-Agent") String userAgent,
                                  @HeaderParam("X-Forwarded-For") String ip,
                                  @HeaderParam("Authorization") String token,
                                  Company req)
    {

        // AUTH
        if (!authService.checkUser(token, ip, userAgent))
            throw new UnauthorizedException("Falha na autenticacao!");

        companyService.CriarEmpresa(req);

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
                                   @HeaderParam("Authorization") String token,
                                   @HeaderParam("X-Forwarded-For") String ip,
                                   @HeaderParam("User-Agent") String userAgent)
    {

        // AUTH
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        authService.checkCompanyPermission(empresas, req.getCompany_id());

        // DB - QUERY
        companyService.CriarFuncionario(req);

        return Response.status(Response.Status.OK)
                                .entity("Usuario criado com sucesso!")
                                .build();
    }

    @GET
    @Path("/list_funcs/{company_id}")
    @Timed(name = "Get-funcs", description = "Latencia para get funcs", unit = MetricUnits.MILLISECONDS, absolute = true)
    public Response list_company_funcs(@QueryParam("page") @DefaultValue("1") int page,
                                        @QueryParam("size") @DefaultValue("10") int size,
                                        @HeaderParam("User-Agent") String userAgent,
                                        @HeaderParam("X-Forwarded-For") String ip,
                                        @PathParam("company_id") long company_id,
                                        @HeaderParam("Authorization") String token)
    {

        // AUTH
        if (!authService.checkCompany(token, ip, company_id, userAgent))
            throw new UnauthorizedException("Nao tem permissao para esta empresa!");

        // DB - QUERY
        List<User> req = companyService.findUsersByCompanyIdPageable(company_id, page, size);

        return Response
                .status(Response.Status.OK)
                .entity(req)
                .build();
    }


    @POST
    @Path("/add_func")
    @Transactional
    public Response add_employee(@HeaderParam("Authorization") String token,
                                 @HeaderParam("X-Forwarded-For") String ip,
                                 @HeaderParam("User-Agent") String userAgent,
                                 AtribEmpresaDTO req)
    {

        // AUTH - TODO: LIMPAR DPS
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        authService.checkCompanyPermission(empresas, req.getCompanyId());

        // DB - QUERY - TODO: OTIMIZAR PARA MENOS QUERIES
        companyService.atribuirEmpresa(req);

        return Response.ok().entity(req).build();
    }
}