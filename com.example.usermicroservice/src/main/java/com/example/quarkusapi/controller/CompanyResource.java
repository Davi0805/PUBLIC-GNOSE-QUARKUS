package com.example.quarkusapi.controller;

import com.example.quarkusapi.DTO.CreateUserAdminRequestDTO;
import com.example.quarkusapi.Exception.BadRequestException;
import com.example.quarkusapi.Exception.ResourceConflictException;
import com.example.quarkusapi.Exception.UnauthorizedException;
import com.example.quarkusapi.model.User;
import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.UserCompany;
import com.example.quarkusapi.model.newEmployee;
import com.example.quarkusapi.service.EmailService;
import com.example.quarkusapi.service.RedisService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
    @Inject
    EmailService emailService;

    // TODO: REFATORAR ESTA MERDA SEPARANDO EM SERVICES ETC
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
        Company existingCompany = Company.find("company_name", companyRequest.company_name).firstResult();
        if (existingCompany != null)
            throw new ResourceConflictException("Empresa ja existe");


        // User ja existe? // CHECAR DEPOIS USERNAME E EMAIL
        User existingUser = User.find("email",
                employeeRequest.getEmail()).firstResult();


        if (existingUser != null)
            throw new ResourceConflictException("Usuario ja existe");


        // Cria empresa
        Company company = new Company();
        company.company_name = companyRequest.company_name;
        company.persist();

        // Cria user
        User user = new User();
        user.fill_User(employeeRequest);
        user.persist();

        // Adiciona userid, company_id e permission na tabela de conexoes
        UserCompany userCompany = new UserCompany();
        userCompany.user = user;
        userCompany.company = company;
        userCompany.permission = "A"; // Admin permission
        userCompany.persist();

        // Hita Serverless func para mandar link de verificacao de email
        // emailService.sendEmailVerification(user.email, user.first_name);


        return Response
                .status(Response.Status.CREATED)
                .entity("User and company created successfully, and user assigned as admin.")
                .build();
    }

    @POST
    @Transactional
    public Response criar_empresa(Company req)
    {
        if (req == null || req.company_name == null) // Adicionar mais checagems apos deixar entity mais cheia
            throw new BadRequestException("Preencha todos os campos!");

        Company existing_company = Company.find("company_name", req.company_name).firstResult();
        if (existing_company != null)
            throw new ResourceConflictException("Empresa ja existe");

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
            throw new BadRequestException("Preencha todos os campos!");

        List<RedisCompanies> empresas = redisService.get_user_companies(token);
        if (empresas.isEmpty() || !empresas.stream()
                                    .anyMatch(empresa -> empresa.getId().getCompanyId() == req.getCompany_id() && empresa.getPermission().equals("A")))
            throw new UnauthorizedException("Nao tem permissao para criar funcionarios nessa empresa");


        User existingUser = User.find("username", req.getUsername()).firstResult();
        Company existingCompany = Company.find("id", req.getCompany_id()).firstResult();
        if (existingUser != null)
            throw new ResourceConflictException("Usuario ja existe");

        if (existingCompany == null)
            throw new BadRequestException("Empresa nao existe");

        
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

        if (empresas.isEmpty() || !credentials)
            throw new UnauthorizedException("Nao tem permissao para acessar esta empresa");

        LOG.infof("Endpoint %s\n", empresas);
        List<User> req = UserCompany.findUsersByCompanyId(company_id);
        if (req == null || req.isEmpty())
            throw new BadRequestException("Empresa nao encontrada");

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
            throw new BadRequestException("Preencha todos os campos");

        if (redisService.validateToken(token))
        {
            List<RedisCompanies> empresas = redisService.get_user_companies(token);
            boolean credentials = empresas.stream()
                                            .anyMatch(empresa -> empresa.getId().getCompanyId() == req.id.getCompanyId()
                                                                && empresa.getPermission().equals("A"));
            if (!credentials)
                throw new UnauthorizedException("Nao tem permissao para acessar esta empresa");
        }
        User user = User.findById(req.id.getUserId());
        Company company = Company.findById(req.id.getCompanyId());
        if (user == null || company == null)
            throw new BadRequestException("Preencha todos os campos");
        
        if (req.permission == null || req.permission.isEmpty())
            throw new jakarta.ws.rs.BadRequestException("Permissao invalida");
        
        req.user = user;
        req.company = company;
        
        req.persist();
        return Response
            .status(Response.Status.OK)
            .entity(req)
            .build();
    }
}