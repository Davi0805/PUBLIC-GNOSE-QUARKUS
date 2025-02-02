package com.example.quarkusapi.service;

import com.example.quarkusapi.Exception.BadRequestException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;

@ApplicationScoped
public class EmailService {
    @ConfigProperty(name = "email.verification.url")
    String FUNCTION_URL;

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // Transformar em Async depois
    public Uni<Void> sendEmailVerificationAsync(String emailAddress, String name, String token) {

        MDC.put("emailAddress", emailAddress);
        MDC.put("name", name);

        // TODO: Substituir por DTO DEPOIS
        String payload = String.format("""
        {
            "to": "%s",
            "name": "%s",
            "token": "%s"
        }
    """, emailAddress, name, token);

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
}