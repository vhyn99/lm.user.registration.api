package com.legalmatch.api.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

@Component
public class ServiceHealthCheckQueryResolver implements GraphQLQueryResolver {
    public String _serviceHealthCheck() {
        return "Service is up and running!";
    }
}


