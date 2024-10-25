package com.example.quarkusapi.utils;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtUtils {
    @Inject
    JWTParser jwtParser;

    public String generateToken(String username)
    {
        Set<String> roles = new HashSet<>();
        roles.add("User");

        return Jwt.issuer("issuer")
                                        .upn(username)
                                        .groups(roles)
                                        .expiresIn(3600)
                                        .sign();
    }

    public String getUsernameFromToken(String token) throws ParseException {
        return jwtParser.parse(token).getName();
    }
}
