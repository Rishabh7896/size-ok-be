package com.sizeok.sizechart.repository;

import com.sizeok.sizechart.domain.SizeChart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SizeChartRepository extends JpaRepository<SizeChart, String> {
    List<SizeChart> findByProductIdAndStatusInOrderByVersionDesc(String productId, Collection<String> statuses);
    List<SizeChart> findByBrandIdAndCategoryIdAndRegionAndProductIdIsNullAndStatusInOrderByVersionDesc(
            String brandId, String categoryId, String region, Collection<String> statuses);
    List<SizeChart> findByBrandIdAndCategoryIdAndProductIdIsNullAndStatusInOrderByVersionDesc(
            String brandId, String categoryId, Collection<String> statuses);
    List<SizeChart> findByBrandIdOrderByVersionDesc(String brandId);
    List<SizeChart> findByProductIdOrderByVersionDesc(String productId);
}
