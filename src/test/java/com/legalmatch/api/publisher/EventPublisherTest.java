package com.legalmatch.api.publisher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestJmsConfig.class)
public class EventPublisherTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    public void testEventPublishing() {
        // Assuming your destination is "myEventQueue"
        eventPublisher.publishEvent("myEventQueue", "Test Message");

        // Add assertions or verifications based on your testing requirements
    }
}

