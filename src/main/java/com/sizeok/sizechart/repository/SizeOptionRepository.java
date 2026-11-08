package com.sizeok.sizechart.repository;

import com.sizeok.sizechart.domain.SizeOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SizeOptionRepository extends JpaRepository<SizeOption, String> {
    List<SizeOption> findBySizeChartIdOrderBySortOrderAsc(String sizeChartId);
    Optional<SizeOption> findBySizeChartIdAndSizeLabelIgnoreCase(String sizeChartId, String sizeLabel);
}
