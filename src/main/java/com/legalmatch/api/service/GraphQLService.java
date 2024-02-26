package com.legalmatch.api.service;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.servlet.internal.GraphQLRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GraphQLService {

    private final GraphQL graphQL;

    @Autowired
    public GraphQLService(GraphQLSchema graphQLSchema) {
        // You need a GraphQLSchema to create a GraphQL instance
        this.graphQL = GraphQL.newGraphQL(graphQLSchema)
                .queryExecutionStrategy(new AsyncExecutionStrategy()) // Choose an execution strategy
                .mutationExecutionStrategy(new AsyncExecutionStrategy()) // Choose an execution strategy
                .build();
    }

    public ExecutionResult execute(String query) {
        // Use a String for the query
        return graphQL.execute(query);
    }

}

