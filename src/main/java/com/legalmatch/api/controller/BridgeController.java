package com.legalmatch.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.legalmatch.api.model.WebhookEntity;
import com.legalmatch.api.model.dao.WebhookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static kotlin.reflect.jvm.internal.impl.builtins.StandardNames.FqNames.throwable;

@RestController
@RequestMapping("/api/v1/bridge") //  Optional base path
@Slf4j
public class BridgeController {
    private RestTemplate restTemplate; // For forwarding requests
    private WebhookRepository webhookRepository;

    @Autowired
    public BridgeController(RestTemplate restTemplate, WebhookRepository webhookRepository) {
        this.restTemplate = restTemplate;
        this.webhookRepository = webhookRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerWebhook(@RequestParam("webhookId") String webhookId,
                                                  @RequestParam("webhookUrl") String webhookUrl) {
        log.info("[BridgeController | Register Webhook] Registering Webhook {} with URL {}", webhookId, webhookUrl);

        // Save the webhook URL to the database
        WebhookEntity webhookEntity = new WebhookEntity(webhookId, webhookUrl);
        webhookRepository.save(webhookEntity);

        log.info("[BridgeController | Register Webhook] Webhook registered successfully");
        return ResponseEntity.ok("Webhook registered successfully");
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhookEvent(@RequestBody String payload,
                                                     @RequestHeader HttpHeaders headers) throws InterruptedException {
        log.info("[BridgeController | Webhook Event] Processing incoming webhook event...");

        // Extract relevant information from the payload and take necessary actions
        List<WebhookEntity> webhookEntities = Lists.newArrayList(webhookRepository.findAll());

        // Retry settings
        int maxRetries = 3; // Maximum number of retry attempts

        // Flag to track whether at least one attempt was successful
        boolean onSuccess = false;

        // Forward the event to registered webhook URLs with retry
        for (WebhookEntity webhookEntity : webhookEntities) {
            String webhookUrl = webhookEntity.getWebhookUrl();
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    log.info("[BridgeController | Webhook Event] Forwarding event to: {} (Attempt {}/{})", webhookUrl, attempt, maxRetries);
                    if ("lm".equalsIgnoreCase(webhookEntity.getWebhookId())) {
                        restTemplate.postForEntity(webhookUrl, payload, String.class);
                    }
                    else if ("ccpm".equalsIgnoreCase(webhookEntity.getWebhookId())) {
                        processGraphQLRequest(payload, webhookUrl);
                    }
                    log.info("[BridgeController | Webhook Event] Event forwarded successfully");
                    onSuccess = true;
                    break; // Break the loop if successful
                } catch (HttpStatusCodeException e) {
                    log.error("[BridgeController | Webhook Event] Error forwarding event to: {} (Attempt {}/{})", webhookUrl, attempt, maxRetries);
                    log.error("[BridgeController | Webhook Event] Root Cause: {}", getRootCause(e).getMessage());
                    Thread.sleep(500);
                } catch (RestClientException e) {
                    log.error("[BridgeController | Webhook Event] General error forwarding event to: {} (Attempt {}/{})", webhookUrl, attempt, maxRetries);
                    log.error("[BridgeController | Webhook Event] Root Cause: {}", getRootCause(e).getMessage());
                    Thread.sleep(500);
                }
            }
        }

        if (onSuccess) {
            log.info("[BridgeController | Webhook Event] Webhook event processed successfully");
            return ResponseEntity.ok("Webhook event processed successfully");
        } else {
            log.error("[BridgeController | Webhook Event] All attempts failed. Webhook event processing failed.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook event processing failed.");
        }
    }

    private ResponseEntity<String> processGraphQLRequest(String payload, String webhookUrl) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode clientInputNode = rootNode;

            // Extract individual fields
            String username = clientInputNode.path("username").asText();
            String email = clientInputNode.path("email").asText();
            String password = clientInputNode.path("password").asText();
            String firstName = clientInputNode.path("firstName").asText();
            String lastName = clientInputNode.path("lastName").asText();
            // Extract other fields as needed

            // Now you can use the extracted values as needed, for example, to perform the GraphQL mutation
            performGraphQLMutation(firstName, lastName, email, webhookUrl);

            return ResponseEntity.ok("Webhook event processed successfully");
        } catch (IOException e) {
            log.error("Error parsing webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook event");
        }
    }

    private void performGraphQLMutation(String firstName, String lastName, String email, String url) {
        String graphqlEndpoint = url;
        String apiKey = "GbXvJk48DSds5sxzWGW0"; // Replace with your API key

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-api-key", apiKey); // Add x-api-key header

        String mutation = String.format(
                "mutation { insertClient(ClientInput: { firstName: \"%s\", lastName: \"%s\", email: \"%s\" }) { id firstName lastName email } }",
                firstName, lastName, email);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode requestJson = objectMapper.createObjectNode().put("query", mutation);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);

        //HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(graphqlEndpoint, requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Handle successful response
            String response = responseEntity.getBody();
            // Process the GraphQL server response if needed
            log.info("GraphQL server response: {}", response);
        } else {
            // Handle unsuccessful response
            log.error("Error from GraphQL server. HTTP status: {}", responseEntity.getStatusCode());
        }
    }

    private String toJsonString(Map<String, Object> map) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting Map to JSON string", e);
        }
    }

    // Utility method to get the root cause of an exception
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}

