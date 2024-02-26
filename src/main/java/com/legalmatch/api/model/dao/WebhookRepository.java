package com.legalmatch.api.model.dao;


import com.legalmatch.api.model.WebhookEntity;
import org.springframework.data.repository.CrudRepository;

public interface WebhookRepository extends CrudRepository<WebhookEntity, String> {
}
