package com.sizeok.identity.repository;

import com.sizeok.identity.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findByUserId(String userId);
}
