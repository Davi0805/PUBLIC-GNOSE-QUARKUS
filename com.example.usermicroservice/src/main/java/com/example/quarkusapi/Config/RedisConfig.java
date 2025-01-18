package com.example.quarkusapi.Config;

import io.quarkus.redis.client.RedisHostsProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.Set;

@ApplicationScoped
public class RedisConfig implements RedisHostsProvider {

    @ConfigProperty(name = "quarkus.redis.hosts")
    String redisUrl;


    @Override
    public Set<URI> getHosts() {
        return Set.of(URI.create(redisUrl));
    }
}