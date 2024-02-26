package com.legalmatch.api.controller;

import com.google.common.collect.Lists;
import com.legalmatch.api.model.WebhookEntity;
import com.legalmatch.api.model.dao.WebhookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                    ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, payload, String.class);
                    log.info("[BridgeController | Webhook Event] Event forwarded successfully");

                    // Process the response here if needed
                    log.info("[BridgeController | Webhook Event] Target URL Response: {}", response.getBody());

                    onSuccess = true;
                    break; // Break the loop if successful
                } catch (HttpStatusCodeException e) {
                    log.error("[BridgeController | Webhook Event] Error forwarding event to: {} (Attempt {}/{})", webhookUrl, attempt, maxRetries);
                    log.error("[BridgeController | Webhook Event] Root Cause: {}", getRootCause(e).getMessage());
                    Thread.sleep(1500);
                } catch (RestClientException e) {
                    log.error("[BridgeController | Webhook Event] General error forwarding event to: {} (Attempt {}/{})", webhookUrl, attempt, maxRetries);
                    log.error("[BridgeController | Webhook Event] Root Cause: {}", getRootCause(e).getMessage());
                    Thread.sleep(1500);
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

    // Utility method to get the root cause of an exception
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
