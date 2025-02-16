package com.example.quarkusapi.controller;

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
    RedisService redisService;
    @Inject
    EmailService emailService;
    @Inject
    AuthService authService;
    @Inject
    UserRepository userRepository;
    @Inject
    UserCompanyRepository userCompanyRepository;
    @Inject
    CompanyRepository companyRepository;

    // TODO: REFATORAR ESTA MERDA SEPARANDO EM SERVICES ETC
    // TODO: ADIONAR FIELD DE TOKEN E CHECAGEM NO REDIS PARA CONVITE
    @POST
    @Path("/create_user_admin")
    @Transactional
    public Response createUserAndAssignAdmin(@Valid CreateUserAdminRequestDTO request) {
        if (request == null || request.getEmployeeRequest() == null || request.getCompanyRequest() == null)
            throw new BadRequestException("Preencha todos os dados validos!");

        // Get do DTO
        newEmployee employeeRequest = request.getEmployeeRequest();
        Company companyRequest = request.getCompanyRequest();

        // Empresa ja existe?
        Company existingCompany = companyRepository.find("company_name", companyRequest.company_name).firstResult();
        if (existingCompany != null)
            throw new ResourceConflictException("Empresa ja existe");


        // User ja existe? // CHECAR DEPOIS USERNAME E EMAIL
        User existingUser = userRepository.find("email",
                employeeRequest.getEmail()).firstResult();


        if (existingUser != null)
            throw new ResourceConflictException("Usuario ja existe");


        // Cria empresa
        Company company = new Company();
        company.company_name = companyRequest.company_name;
        companyRepository.persist(company);
        companyRepository.flush();

        // Cria user
        User user = new User();
        user.fill_User(employeeRequest);
        userRepository.persist(user);
        userRepository.flush();

        // Adiciona userid, company_id e permission na tabela de conexoes
        UserCompany userCompany = new UserCompany();
        LOG.info(user + "   |    " + company);
        userCompany.id = new UserCompanyId(user.id, company.id);
        userCompany.user = user;
        userCompany.company = company;
        userCompany.permission = "A"; // Admin permission
        userCompanyRepository.persist(userCompany);

        // TODO: Modificar sendgrid logic
        // Hita Serverless func para mandar link de verificacao de email
        String token = redisService.saveEmail(user.id);
        emailService.sendEmailVerificationAsync(new EmailVerificationRequest(user.email, user.first_name, token))
                .subscribe().with(ignored -> {}, failure -> {});;

        return Response
                .status(Response.Status.CREATED)
                .entity("User and company created successfully, and user assigned as admin.")
                .build();
    }

    @POST
    @Transactional
    public Response criar_empresa(@HeaderParam("User-Agent") String userAgent,
                                  @HeaderParam("X-Forwarded-For") String ip,
                                  @HeaderParam("Authorization") String token,
                                  Company req)
    {
        if (token != null)
            token = token.substring(7);

        // AUTH
        if (!authService.checkUser(token, ip, userAgent))
            throw new UnauthorizedException("Falha na autenticacao!");

        // CHECAGEM
        Company existing_company = companyRepository.find("company_name", req.company_name).firstResult();
        if (existing_company != null)
            throw new ResourceConflictException("Empresa ja existe");

        // SALVA TRANSACTION
        companyRepository.persist(req);

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
        if (token != null)
            token = token.substring(7);

        // AUTH
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        if (empresas.stream().noneMatch(empresa ->
                empresa.getId().getCompanyId() == req.getCompany_id() &&
                        empresa.getPermission().equals("A")))
            throw new UnauthorizedException("Nao faz parte dessa empresa ou nao tem permissao!");

        // CHECAGEM - TODO: OTIMIZAR QUERY
        User existingUser = userRepository.find("username", req.getUsername()).firstResult();
        Company existingCompany = companyRepository.find("id", req.getCompany_id()).firstResult();
        if (existingUser != null || existingCompany != null)
            throw new ResourceConflictException("Usuario ou empresa ja existe");

        //TODO: OTIMIZAR QUERY
        // DB - QUERY
        User user_transc = new User().fill_User(req);
        userRepository.persist(user_transc);

        UserCompany relation = new UserCompany();
        relation.user = user_transc;
        relation.company = existingCompany;
        relation.permission = req.getCompany_permission();
        userCompanyRepository.persist(relation);

        return Response
                    .status(Response.Status.OK)
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
        if (token != null)
            token = token.substring(7);

        // AUTH
        if (!authService.checkCompany(token, ip, company_id, userAgent))
            throw new UnauthorizedException("Nao tem permissao para esta empresa!");

        // DB - QUERY
        List<User> req = userRepository.find("company_id", company_id).page(page - 1, size).list();
        if (req == null || req.isEmpty())
            throw new NotFoundException("Empresa nao encontrada");

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
                                 UserCompany req)
    {
        if (token != null)
            token = token.substring(7);

        // AUTH - TODO: LIMPAR DPS
        List<RedisCompanies> empresas = authService.check(token, ip, userAgent);
        if (empresas.stream().noneMatch(empresa ->
                empresa.getId().getCompanyId() == req.id.getCompanyId() &&
                        empresa.getPermission().equals("A")))
            throw new UnauthorizedException("Nao faz parte dessa empresa ou nao tem permissao!");

        // DB - QUERY - TODO: OTIMIZAR PARA MENOS QUERIES
        User user = userRepository.findById(req.id.getUserId());
        Company company = companyRepository.findById(req.id.getCompanyId());
        if (user == null || company == null)
            throw new BadRequestException("Preencha todos os campos");

        // ATRIBUI
        req.user = user;
        req.company = company;

        // CHECAGEM
        if (req.permission == null || req.permission.isEmpty())
            throw new BadRequestException("Permissao invalida");
        
        userCompanyRepository.persist(req);

        return Response.ok().entity(req).build();
    }
}