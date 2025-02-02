package com.example.quarkusapi.service;

import com.example.quarkusapi.model.RedisCompanies;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.List;

// Name: AuthService
// Function: Checar/Validar ip, token e permissoes de empresa
// utilizando redis e rate limit policy para evitar bruteforces


@ApplicationScoped
public class AuthService
{
    @Inject
    RedisClient redisClient;

    @Inject
    ObjectMapper objectMapper;

    private static final String prefixo = "RATE-LIMIT:";
    private static final int auth_attempt = 5;
    private static final int block_time = 300; //5 minutos

    private static final Logger LOG = Logger.getLogger(AuthService.class);


    // Feito para retornar as empresas do usuario, se existirem
    public List<RedisCompanies> check(String token, String clientIp, String userAgent)
    {
        try {
            MDC.put("clientIp", clientIp);
            MDC.put("Service", "AuthService");
            MDC.put("userAgent", userAgent);
            MDC.put("Auth.method", "JWT");

            // Utiliza pipeline para fetch de duas keys ao mesmo tempo
            if(token == null) {
                LOG.warn("Token nao definido");
                return List.of();
            }

            Response result = redisClient.mget(List.of(prefixo + clientIp, token));



            int attempts = result.get(0) != null ? Integer.parseInt(result.get(0).toString()) : 0;

            if (attempts >= auth_attempt) {
                MDC.put("Tentativa", String.valueOf(attempts));
                if (attempts > auth_attempt)
                    LOG.warn("Usuario bloqueado tentou fazer uma requisicao");
                else if (attempts == auth_attempt)
                    LOG.warn("IP BLOQUEADO");

                return List.of();
            }

            if (result.get(1) != null)
            {
                List<RedisCompanies> empresas = objectMapper.readValue(result.get(1).toString(), new TypeReference<List<RedisCompanies>>() {
                });

                // LOG
                Long userId = empresas.get(0).getId().getUserId();
                MDC.put("userId", String.valueOf(userId));
                return empresas;
            }
            else {
                RegisterAuthAttempt(clientIp, attempts); // Aumenta n de tentativas relacionado ao ip
                LOG.warn("Falha na autenticacao: " + attempts + " tentativas");
                return List.of();
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    // Feito para checar se o usuario faz parte da empresa que esta tentando acessar
    public Boolean checkCompany(String token, String clientIp, Long companyId, String userAgent)
    {
        try {
            MDC.put("clientIp", clientIp);
            MDC.put("company_id", companyId);
            MDC.put("Service", "AuthService");
            MDC.put("userAgent", userAgent);
            MDC.put("Auth.method", "JWT");


            // Utiliza pipeline para fetch de duas keys ao mesmo tempo
            if(token == null) {
                LOG.warn("Token nao definido");
                return false;
            }

            Response result = redisClient.mget(List.of(prefixo + clientIp, token));



            int attempts = result.get(0) != null ? Integer.parseInt(result.get(0).toString()) : 0;

            if (attempts >= auth_attempt) {
                MDC.put("Tentativa", String.valueOf(attempts));
                if (attempts > auth_attempt)
                    LOG.warn("Usuario bloqueado tentou fazer uma requisicao");
                else if (attempts == auth_attempt)
                    LOG.warn("IP BLOQUEADO");

                return false;
            }

            // So registra suspeita de brute force em caso de token nao encontrado
            if (result.get(1) != null) {
                List<RedisCompanies> empresas = objectMapper.readValue(result.get(1).toString(), new TypeReference<List<RedisCompanies>>() {
                });
                Long userId = empresas.get(0).getId().getUserId();
                MDC.put("userId", String.valueOf(userId));
                if (empresas.stream().anyMatch(empresa -> empresa.getId().getCompanyId() == companyId))
                    return true;
                // SUSPEITA
                LOG.warn("Possivel Scrapping detectado " + attempts + " tentativas");
                return false;
            } else {
                RegisterAuthAttempt(clientIp, attempts); // Aumenta n de tentativas relacionado ao ip
                LOG.warn("Falha na autenticacao: " + attempts + "tentativas");
                return false;
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }


    public void RegisterAuthAttempt(String ip, int attempts)
    {
        attempts++;
        redisClient.setex(prefixo + ip, String.valueOf(block_time), String.valueOf(attempts));
    }

    // Feito para checar se o usuario esta logado mesmo sem ter empresa
    public Boolean checkUser(String token, String clientIp, String userAgent)
    {
        try {
            MDC.put("clientIp", clientIp);
            MDC.put("Service", "AuthService");
            MDC.put("userAgent", userAgent);
            MDC.put("Auth.method", "JWT");


            // Utiliza pipeline para fetch de duas keys ao mesmo tempo
            if(token == null) {
                LOG.warn("Token nao definido");
                return false;
            }

            Response result = redisClient.mget(List.of(prefixo + clientIp, token));



            int attempts = result.get(0) != null ? Integer.parseInt(result.get(0).toString()) : 0;

            if (attempts >= auth_attempt) {
                MDC.put("Tentativa", String.valueOf(attempts));
                if (attempts > auth_attempt)
                    LOG.warn("Usuario bloqueado tentou fazer uma requisicao");
                else if (attempts == auth_attempt)
                    LOG.warn("IP BLOQUEADO");

                return false;
            }

            // So registra suspeita de brute force em caso de token nao encontrado
            if (result.get(1) != null) {
                    return true;
            } else {
                RegisterAuthAttempt(clientIp, attempts); // Aumenta n de tentativas relacionado ao ip
                LOG.warn("Falha na autenticacao: " + attempts + "tentativas");
                return false;
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }
}
