package com.example.quarkusapi.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AtribEmpresaDTO {
    private String email;
    private Long companyId;
    private String permission;

    @JsonCreator
    public AtribEmpresaDTO(@JsonProperty("email") String email, @JsonProperty("companyId") Long companyId, @JsonProperty("permission") String permission) {
        this.email = email;
        this.companyId = companyId;
        this.permission = permission;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

}
