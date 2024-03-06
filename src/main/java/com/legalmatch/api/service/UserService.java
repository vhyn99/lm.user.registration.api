package com.legalmatch.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalmatch.api.model.User;
import com.legalmatch.api.model.dao.UserRepository;
import com.legalmatch.api.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class UserService {
    private UserRepository userRepository;
    private EventPublisher eventPublisher;
    private ObjectMapper objectMapper; // Add Jackson ObjectMapper
    private RestTemplate restTemplate;
    private HttpServletRequest request;

    @Autowired
    public UserService(UserRepository userRepository, EventPublisher eventPublisher,
                       ObjectMapper objectMapper, RestTemplate restTemplate, HttpServletRequest request) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.request = request;
    }

    public void registerUser(User user) throws JsonProcessingException {
        //userRepository.save(user);
        this.forward(user);
        //String userJson = objectMapper.writeValueAsString(user);
        //eventPublisher.publishEvent("client.registration.queue", userJson);

    }
    protected void forward(User user) throws JsonProcessingException {
        String userJson = objectMapper.writeValueAsString(user);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        String baseUrl = getBaseUrl();
        String bridgeUrl = baseUrl + "/api/v1/bridge/webhook";

        // You might need to customize the headers based on your requirements
        HttpEntity<String> requestEntity = new HttpEntity<>(userJson, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(bridgeUrl, requestEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                log.info("User registration payload forwarded successfully");
            } else {
                log.error("Error forwarding user registration payload. HTTP status: {}", responseEntity.getStatusCode());
                user.setFailForward(true);
                userRepository.save(user);
            }
        } catch (RestClientException e) {
            log.error("Error forwarding user registration payload", e);
            user.setFailForward(true);
            userRepository.save(user);
        }
    }

    private String getBaseUrl() {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        return scheme + "://" + serverName + (serverPort != 80 ? ":" + serverPort : "");
    }

}
