package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.AtribEmpresaDTO;
import com.example.quarkusapi.DTO.CreateUserAdminRequestDTO;
import com.example.quarkusapi.Repositories.CompanyRepository;
import com.example.quarkusapi.Repositories.UserCompanyRepository;
import com.example.quarkusapi.Repositories.UserRepository;
import com.example.quarkusapi.model.*;
import com.example.quarkusapi.Exception.*;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import com.example.quarkusapi.Exception.*;
import org.hibernate.exception.ConstraintViolationException;


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

    public void atribuirEmpresa(AtribEmpresaDTO req) throws BadRequestException{
        User user = userRepository.find("email = ?1", req.getEmail()).firstResult();
        Company company = companyRepository.findById(req.getCompanyId());
        if (user == null || company == null)
            throw new BadRequestException("Preencha todos os campos");
        else if (req.getPermission() == null || req.getPermission().length() != 1)
            throw new BadRequestException("Permissao invalida");

        UserCompany relation = new UserCompany(new UserCompanyId(user.id, company.id), user, company, req.getPermission());

        try {
            userCompanyRepository.persist(relation);
        } catch (PersistenceException e) {
            Log.error(e.getCause());
            throw new ResourceConflictException("Usuario ja esta vinculado a empresa");
        } catch (Exception e) {
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro inesperado ao atribuir empresa");
        }
    }

    public void CriarFuncionario(newEmployee req) throws ResourceConflictException {

        User existingUser = userRepository.find("username", req.getUsername()).firstResult();
        Company existingCompany = companyRepository.find("id", req.getCompany_id()).firstResult();
        if (existingUser != null || existingCompany != null)
            throw new ResourceConflictException("Usuario ou empresa ja existe");

        User user_transc = new User().fill_User(req);
        UserCompany relation = new UserCompany(user_transc, existingCompany, req.getCompany_permission());

        try {
            userRepository.persist(user_transc);
            userCompanyRepository.persist(relation);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException)
                throw new ResourceConflictException("Usuario ja existe");
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro ao criar funcionario");
        } catch (Exception e) { // Redundancia por seguranca
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro inesperado ao criar funcionario");
        }
    }

    public void CriarEmpresa(Company req) throws ResourceConflictException {
        try {
            companyRepository.persist(req);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new ResourceConflictException("Empresa ja existe");
            }
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro ao criar empresa");
        } catch (Exception e) { // Redundancia por seguranca
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro inesperado ao criar empresa");
        }
    }

    public User CreateUserAndEmpresa(CreateUserAdminRequestDTO request) throws ResourceConflictException {

        newEmployee employeeRequest = request.getEmployeeRequest();
        Company companyRequest = request.getCompanyRequest();

        try {
            Company company = new Company(companyRequest.company_name);
            companyRepository.persist(company);
            companyRepository.flush();

            User user = new User();
            user.fill_User(employeeRequest);
            userRepository.persist(user);
            userRepository.flush(); // Necessario para obter o id do usuario

            UserCompany userCompany = new UserCompany(new UserCompanyId(
                    user.id, company.id),
                    user, company, "A");
            userCompanyRepository.persist(userCompany);
            return user;
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
                String constraintName = cve.getConstraintName();

                if (constraintName != null) {
                    if (constraintName.contains("companies")) {
                        throw new ResourceConflictException("Empresa já existe com este nome");
                    } else if (constraintName.contains("users_username_key")) {
                        throw new ResourceConflictException("Usuário já existe com este nome de usuário");
                    } else if (constraintName.contains("users_email_key")) {
                        throw new ResourceConflictException("Email já está em uso");
                    }
                }
                throw new ResourceConflictException("Erro de validação: registro duplicado");
            }
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro ao criar usuario e empresa");
        } catch (Exception e) {
            Log.error(e.getCause());
            throw new InternalServerErrorException("Erro inesperado ao criar usuario e empresa");
        }
    }
}
