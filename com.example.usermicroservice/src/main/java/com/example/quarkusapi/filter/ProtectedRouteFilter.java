package com.example.quarkusapi.filter;

import com.example.quarkusapi.service.RedisService;
import com.example.quarkusapi.utils.JwtUtils;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

@Provider
@ProtectedRoute
@Priority(Priorities.AUTHENTICATION)
public class ProtectedRouteFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(ProtectedRouteFilter.class);

    @Inject
    RedisService redisService;

    @Inject
    JwtUtils jwtUtil;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                if (!redisService.validateToken(token)) {
                    LOG.warn("Token not found in Redis or invalid");
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                }
            } catch (Exception e) {
                LOG.error("Token validation failed", e);
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            LOG.warn("Authorization header missing or invalid");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
