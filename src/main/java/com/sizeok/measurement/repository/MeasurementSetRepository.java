package com.sizeok.measurement.repository;

import com.sizeok.measurement.domain.MeasurementSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeasurementSetRepository extends JpaRepository<MeasurementSet, String> {
    List<MeasurementSet> findByProfileIdOrderByVersionDesc(String profileId);
    Optional<MeasurementSet> findFirstByProfileIdAndStatusOrderByVersionDesc(String profileId, String status);
}
