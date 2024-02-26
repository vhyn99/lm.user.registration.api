package com.legalmatch.api.resolver;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.legalmatch.api.model.User;
import com.legalmatch.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMutation implements GraphQLMutationResolver {

    UserService userService;

    @Autowired
    public UserMutation(UserService userService) {
        this.userService = userService;
    }

    public String registerUser(User userInput) throws JsonProcessingException {
        userService.registerUser(userInput);
        return "User Registration Initiated";
    }
}
