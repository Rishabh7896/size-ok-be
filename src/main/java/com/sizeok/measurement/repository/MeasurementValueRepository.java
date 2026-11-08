package com.sizeok.measurement.repository;

import com.sizeok.measurement.domain.MeasurementValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, String> {
    List<MeasurementValue> findByMeasurementSetIdOrderByDimensionCodeAsc(String measurementSetId);
}
