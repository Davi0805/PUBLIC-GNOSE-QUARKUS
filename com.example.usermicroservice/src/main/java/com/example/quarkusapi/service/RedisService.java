package com.example.quarkusapi.service;

import io.quarkus.redis.client.RedisClient;
import com.example.quarkusapi.model.UserCompany;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import com.example.quarkusapi.Exception.BadRequestException;

import java.util.concurrent.CompletionException;
import java.net.UnknownHostException;

import com.example.quarkusapi.model.RedisCompanies;

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
    public String saveEmail(Long userId)
    {
        try {
            // Gera token que sera utilizado para verificar email
            String token = UUID.randomUUID().toString();
            String response = redisClient.setex(token, String.valueOf(604800), userId.toString()).toString();
            LOG.info("Token validacao email: " + token + " response: " + response);
            if (Objects.equals(response, "OK"))
                return token;
            else throw new BadRequestException("Token invalido");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Falha ao cadastrar email");
        }
    }

    @SuppressWarnings("deprecation")
    public Long verifyEmailToken(String token)
    {
        Response response = redisClient.get(token);
        // TODO: Adicionar verificacao
        Long userId = Long.parseLong(response.toString());
        return userId;
    }

    @SuppressWarnings("deprecation")
    public boolean saveToken(String token, Set<UserCompany> userCompanies) {
        try {
            String userCompaniesjson = objectMapper.writeValueAsString(userCompanies);

            String response = redisClient.setex(token, String.valueOf(3600),userCompaniesjson).toString();
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
    public List<RedisCompanies> get_user_companies(String token) {
        try {
            LOG.infof("Retrieving companies for token: %s", token);
            
            Response response = redisClient.get(token);
            
            if (response == null) {
                LOG.info("No companies found for the given token");
                return null;
            }

            String userCompaniesJson = response.toString();

            List<RedisCompanies> userCompanies = objectMapper.readValue(userCompaniesJson, new TypeReference<List<RedisCompanies>>() {
            });
            
            if (userCompaniesJson == null || userCompaniesJson.isEmpty()) {
                LOG.info("Empty companies data for the token");
                return null;
            }

            return userCompanies;

        } catch (Exception e) {
            LOG.error("Unexpected error retrieving user companies", e);
            return null;
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