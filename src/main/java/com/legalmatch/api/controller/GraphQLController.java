package com.legalmatch.api.controller;

import com.legalmatch.api.service.GraphQLService;
import graphql.ExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/graphql")  // or another path where you expose GraphQL
public class GraphQLController {

    @Autowired
    private GraphQLService graphQLService;

    @PostMapping
    public ResponseEntity<?> executeQuery(@RequestBody Map<String, Object> requestBody) {
        // Extract the GraphQL query from the request body
        String query = (String) requestBody.get("query");
        // Assuming you have a GraphQLService to handle GraphQL queries
        ExecutionResult executionResult = graphQLService.execute(query);

        if (executionResult.getErrors().isEmpty()) {
            return ResponseEntity.ok(executionResult.getData());
        } else {
            return ResponseEntity.badRequest().body(executionResult.getErrors());
        }
    }
}

