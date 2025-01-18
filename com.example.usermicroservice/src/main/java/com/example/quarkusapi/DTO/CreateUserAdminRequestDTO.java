package com.example.quarkusapi.DTO;

import com.example.quarkusapi.model.Company;
import com.example.quarkusapi.model.newEmployee;
import jakarta.validation.Valid;

public class CreateUserAdminRequestDTO {

    @Valid
    public newEmployee employeeRequest;

    @Valid
    public Company companyRequest;

    // Constructor, Getters, and Setters (can be generated by your IDE)

    public newEmployee getEmployeeRequest() {
        return employeeRequest;
    }

    public void setEmployeeRequest(newEmployee employeeRequest) {
        this.employeeRequest = employeeRequest;
    }

    public Company getCompanyRequest() {
        return companyRequest;
    }

    public void setCompanyRequest(Company companyRequest) {
        this.companyRequest = companyRequest;
    }
}