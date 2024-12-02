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
            String response = redisClient.set(Arrays.asList(username, token)).toString();
            LOG.infof("STATUS REDIS: %s", response);
            return "OK".equals(response);
        } catch (CompletionException e) {
            // Handle known issue related to Redis connection failure
            if (e.getCause() instanceof UnknownHostException) {
                LOG.error("Redis server not reachable. Please check the connection.", e);
                return false; // Return false or a suitable response to indicate failure
            }
            // Handle other cases of CompletionException
            LOG.error("Unexpected error while saving token to Redis", e);
            return false;
        } catch (Exception e) {
            // Catch other unexpected errors and log them
            LOG.error("Unexpected error occurred while saving token", e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean validateToken(String username, String token) {
        LOG.infof("VALIDACAO TOKEN: %s", username);
        Response response = redisClient.get(username);
        if (response != null) {
            LOG.infof("Token ENCONTRADO: %s", response.toString());
            return response.toString().equals(token);
        } else {
            LOG.info("Token NAO ENCONTRADO");
            return false;
        }
    }
}