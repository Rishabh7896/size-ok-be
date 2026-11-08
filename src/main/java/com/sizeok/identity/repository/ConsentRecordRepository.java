package com.sizeok.identity.repository;

import com.sizeok.identity.domain.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, String> {
    Optional<ConsentRecord> findByUserId(String userId);
}
