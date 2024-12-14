package com.example.quarkusapi.service;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.net.UnknownHostException;

@ApplicationScoped
public class RedisService {

    private static final Logger LOG = Logger.getLogger(RedisService.class);

    @SuppressWarnings("deprecation")
    @Inject
    RedisClient redisClient;

    @SuppressWarnings("deprecation")
    public boolean saveToken(String username, String token) {
        try {
            LOG.infof("TOKEN ARMAZENADO: %s", username);
            String response = redisClient.set(Arrays.asList(token, username)).toString();
            LOG.infof("STATUS REDIS: %s", response);
            return "OK".equals(response);
        } catch (CompletionException e) {
            if (e.getCause() instanceof UnknownHostException) {
                LOG.error("Redis server not reachable. Please check the connection.", e);
                return false;
            }
            LOG.error("Unexpected error while saving token to Redis", e);
            return false;
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while saving token", e);
            return false;
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
    public boolean validateToken(String username, String token) {
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