package com.example.quarkusapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCompanyId implements Serializable {

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "company_id")
    public Long companyId;

    public UserCompanyId() {}

    public UserCompanyId(Long userId, Long companyId) {
        this.userId = userId;
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCompanyId that = (UserCompanyId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, companyId);
    }
}