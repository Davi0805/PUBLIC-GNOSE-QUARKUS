package com.example.quarkusapi.service;

import com.example.quarkusapi.Exception.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class EmailService {
    @ConfigProperty(name = "email.verification.url")
    String FUNCTION_URL;

    // Transformar em Async depois
    public void sendEmailVerification(String emailAddress, String name, String token) {

        // TODO: Substituir por DTO DEPOIS
        String payload = String.format("""
            {
                "to": "%s",
                "name": "%s",
                "token": "%s"
            }
        """, emailAddress, name, token);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FUNCTION_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Loga informações úteis se necessário
            if (response.statusCode() != 202) {
                throw new BadRequestException("Erro no serverless" + response.statusCode() + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar email de verificação", e);
        }
    }
}