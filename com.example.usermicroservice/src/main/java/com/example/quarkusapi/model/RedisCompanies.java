package com.example.quarkusapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RedisCompanies {
    private Id id;
    private String permission;
    private String companyName;

    @JsonCreator
    public RedisCompanies(
            @JsonProperty("id") Id id,
            @JsonProperty("permission") String permission,
            @JsonProperty("companyName") String companyName)
    {
        this.id = id;
        this.permission = permission;
        this.companyName = companyName;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "RedisCompanies{" +
                "id=" + id +
                ", permission='" + permission + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }

    public static class Id {
        private long userId;
        private long companyId;

        public Id(Long userId, Long companyId) {
            this.userId = userId;
            this.companyId = companyId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(long companyId) {
            this.companyId = companyId;
        }

        @Override
        public String toString() {
            return "Id{" +
                    "userId=" + userId +
                    ", companyId=" + companyId +
                    '}';
        }
    }
}
