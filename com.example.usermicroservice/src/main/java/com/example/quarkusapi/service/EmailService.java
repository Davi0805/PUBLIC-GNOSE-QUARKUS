package com.example.quarkusapi.service;

import com.example.quarkusapi.DTO.EmailVerificationRequest;
import com.example.quarkusapi.Exception.BadRequestException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@ApplicationScoped
public class EmailService {
    @ConfigProperty(name = "email.verification.url")
    String FUNCTION_URL;

    @Inject
    RedisService redisService;

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // Transformar em Async depois
    public Uni<Void> sendEmailVerificationAsync(EmailVerificationRequest req) {


        MDC.put("emailAddress", req.getEmail());
        MDC.put("name", req.getName());

        String token = redisService.saveEmail(req.getId());

        // TODO: Substituir por DTO DEPOIS
        String payload = String.format("""
        {
            "to": "%s",
            "name": "%s",
            "token": "%s"
        }
    """, req.getEmail(), req.getName(), token);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FUNCTION_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return Uni.createFrom().completionStage(
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        ).onItem().invoke(response -> {
            if (response.statusCode() != 202) {
                LOGGER.warn("Erro no serverless: " + response.statusCode() + " " + response.body());
            }
        }).onFailure().invoke(ex -> {
            LOGGER.error("Falha na api ao enviar email de verificação", ex);
        }).replaceWithVoid();
    }

//    public CompletableFuture<Void> sendEmailVerificationAsync(String emailAddress, String name, String token) {
//        return CompletableFuture.runAsync(() -> {
//            MDC.put("emailAddress", emailAddress);
//        MDC.put("name", name);
//
//        // TODO: Substituir por DTO DEPOIS
//        String payload = String.format("""
//        {
//            "to": "%s",
//            "name": "%s",
//            "token": "%s"
//        }
//    """, emailAddress, name, token);
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(FUNCTION_URL))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(payload))
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//        }, managedExecutor);
//    }


}