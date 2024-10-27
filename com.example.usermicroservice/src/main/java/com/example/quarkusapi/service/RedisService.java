package com.example.quarkusapi.service;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Arrays;

@ApplicationScoped
public class RedisService {

    private static final Logger LOG = Logger.getLogger(RedisService.class);

    @SuppressWarnings("deprecation")
    @Inject
    RedisClient redisClient;

    @SuppressWarnings("deprecation")
    public void saveToken(String username, String token) {
        LOG.infof("Saving token for user: %s", username);
        redisClient.set(Arrays.asList(username, token));
    }

    @SuppressWarnings("deprecation")
    public boolean validateToken(String username, String token) {
        LOG.infof("Validating token for user: %s", username);
        Response response = redisClient.get(username);
        if (response != null) {
            LOG.infof("Token found in Redis: %s", response.toString());
            return response.toString().equals(token);
        } else {
            LOG.info("Token not found in Redis");
            return false;
        }
    }
}