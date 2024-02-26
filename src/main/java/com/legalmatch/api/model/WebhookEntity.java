package com.legalmatch.api.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.repository.CrudRepository;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "webhook_register")
public class WebhookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "webhook_id", length = 100)
    private String webhookId;

    @Column(nullable = false, unique = true, name = "webhook_url", length = 255)
    private String webhookUrl;

    public WebhookEntity(String webhookId, String webhookUrl) {
        this.webhookId = webhookId;
        this.webhookUrl = webhookUrl;
    }
}


