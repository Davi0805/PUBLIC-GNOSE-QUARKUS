package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.CreateUserAdminRequestDTO;
import com.example.quarkusapi.Repositories.CompanyRepository;
import com.example.quarkusapi.Repositories.UserCompanyRepository;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Id;
import jakarta.ws.rs.NotFoundException;
import com.example.quarkusapi.Exception.*;



import java.util.List;

@ApplicationScoped
public class CompanyService {
    @Inject
    UserRepository userRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UserCompanyRepository userCompanyRepository;

    // TODO: SUBSTITUIR POR DTO SEM CREDENCIAIS
    public List<User> findUsersByCompanyIdPageable(Long companyId, Integer page, Integer size) throws NotFoundException {
        List<User> req = userCompanyRepository.findUsersByCompanyId(companyId);
        if (req == null || req.isEmpty())
            throw new NotFoundException("Empresa nao encontrada");
        return req;
    }

    public void atribuirEmpresa(UserCompany req) throws BadRequestException{
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
    }

    public void CriarFuncionario(newEmployee req) throws ResourceConflictException {

        // TODO: OTIMIZAR QUERY
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
    }

    public void CriarEmpresa(Company req) throws ResourceConflictException {
        Company existingCompany = companyRepository.find("name", req.company_name).firstResult();
        if (existingCompany != null)
            throw new ResourceConflictException("Empresa ja existe");

        companyRepository.persist(req);
    }

    public User CreateUserAndEmpresa(CreateUserAdminRequestDTO request) throws ResourceConflictException {
        // Get do DTO
        newEmployee employeeRequest = request.getEmployeeRequest();
        Company companyRequest = request.getCompanyRequest();

        // Empresa ja existe?
        Company existingCompany = companyRepository.find("company_name", companyRequest.company_name).firstResult();
        if (existingCompany != null)
            throw new ResourceConflictException("Empresa ja existe");

        // User ja existe? // CHECAR DEPOIS USERNAME E EMAIL
        User existingUser  = userRepository.find("username = ?1 or email = ?2", employeeRequest.getUsername(), employeeRequest.getEmail()).firstResult();

        if (existingUser != null)
            throw new ResourceConflictException(existingUser.username.equals(employeeRequest.getUsername()) ?
                    "Nome de usuario ja existe" : "Email ja existe");


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
        userCompany.id = new UserCompanyId(user.id, company.id);
        userCompany.user = user;
        userCompany.company = company;
        userCompany.permission = "A"; // Admin permission
        userCompanyRepository.persist(userCompany);

        return user;
    }
}
