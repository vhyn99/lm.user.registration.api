package com.legalmatch.api.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

@Configuration
public class TestJmsConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean
    public EventPublisher eventPublisher(JmsTemplate jmsTemplate) {
        return new EventPublisher(jmsTemplate);
    }
}

