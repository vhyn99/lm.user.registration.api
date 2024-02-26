package com.legalmatch.api.publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public EventPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publishEvent(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
        System.out.println("Event published: " + message);
    }
}

