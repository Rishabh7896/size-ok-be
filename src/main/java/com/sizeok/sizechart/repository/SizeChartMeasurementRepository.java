package com.sizeok.sizechart.repository;

import com.sizeok.sizechart.domain.SizeChartMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SizeChartMeasurementRepository extends JpaRepository<SizeChartMeasurement, String> {
    List<SizeChartMeasurement> findBySizeOptionId(String sizeOptionId);
    List<SizeChartMeasurement> findBySizeOptionIdIn(Collection<String> sizeOptionIds);
}
