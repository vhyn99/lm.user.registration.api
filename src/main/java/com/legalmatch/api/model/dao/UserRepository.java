package com.legalmatch.api.model.dao;

import com.legalmatch.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

