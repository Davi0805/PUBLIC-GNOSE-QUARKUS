package com.example.quarkusapi.service;

import io.quarkus.redis.client.RedisClient;
import com.example.quarkusapi.model.UserCompany;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.type.TypeReference;

@ApplicationScoped
public class RedisService {

    private static final Logger LOG = Logger.getLogger(RedisService.class);

    @SuppressWarnings("deprecation")
    @Inject
    RedisClient redisClient;

    @Inject
    private ObjectMapper objectMapper;

    @SuppressWarnings("deprecation")
    public boolean saveToken(String token, Set<UserCompany> userCompanies) {
        try {
            String userCompaniesjson = objectMapper.writeValueAsString(userCompanies);

            String response = redisClient.set(Arrays.asList(token, userCompaniesjson)).toString();
            LOG.infof("Companies: %s", userCompaniesjson);
            LOG.infof("STATUS REDIS: %s", response);
            return "OK".equals(response);
        } catch (CompletionException e) {
            if (e.getCause() instanceof UnknownHostException) {
                LOG.error("Redis server not reachable. Please check the connection.", e);
                return false;
            }
            LOG.error("Unexpected error while saving token to Redis", e);
            return false;
        } catch (JsonProcessingException e) {
            // Erro de conversao das companies para json
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while saving token", e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public List<UserCompany> get_user_companies(String token) {
        try {
            LOG.infof("Retrieving companies for token: %s", token);
            
            Response response = redisClient.get(token);
            
            if (response == null) {
                LOG.info("No companies found for the given token");
                return new ArrayList<>();
            }

            String userCompaniesJson = response.toString();
            
            if (userCompaniesJson == null || userCompaniesJson.isEmpty()) {
                LOG.info("Empty companies data for the token");
                return new ArrayList<>();
            }

            List<UserCompany> userCompanies = objectMapper.readValue(
                userCompaniesJson, 
                new TypeReference<List<UserCompany>>() {}
            );

            LOG.infof("Retrieved %d companies for token", userCompanies.size());
            return userCompanies;

        } catch (JsonProcessingException e) {
            LOG.error("Error parsing companies JSON", e);
            return new ArrayList<>();
        } catch (Exception e) {
            LOG.error("Unexpected error retrieving user companies", e);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("deprecation")
    public boolean deleteToken(String token)
    {
        try {
            LOG.infof("TOKEN REQUISITADO: %s\n", token);
            Response response = redisClient.get(token);
            if (response != null)
            {
                String error = redisClient.del(Arrays.asList(token)).toString();
                return "OK".equals(error);
            }
            LOG.infof("RESPOSTA REDIS: %s\n", response);
            return true;
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while saving token", e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean validateToken(String token) {
        LOG.infof("VALIDACAO TOKEN: %s", token);
        Response response = redisClient.get(token);
        if (response != null) {
            LOG.infof("Token ENCONTRADO: %s", response.toString());
            return true;
        } else {
            LOG.info("Token NAO ENCONTRADO");
            return false;
        }
    }
}