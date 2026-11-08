package com.sizeok.sizechart.service;

import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.common.util.UnitConverter;
import com.sizeok.sizechart.controller.SizeChartDtos.ChartDimensionRangeDto;
import com.sizeok.sizechart.controller.SizeChartDtos.CreateSizeChartRequest;
import com.sizeok.sizechart.controller.SizeChartDtos.SizeChartResponse;
import com.sizeok.sizechart.controller.SizeChartDtos.SizeOptionDto;
import com.sizeok.sizechart.domain.SizeChart;
import com.sizeok.sizechart.domain.SizeChartMeasurement;
import com.sizeok.sizechart.domain.SizeOption;
import com.sizeok.sizechart.repository.SizeChartMeasurementRepository;
import com.sizeok.sizechart.repository.SizeChartRepository;
import com.sizeok.sizechart.repository.SizeOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SizeChartService {

    private static final List<String> AUTHORITATIVE_STATUSES = List.of("ACTIVE", "VERIFIED");

    private final SizeChartRepository sizeChartRepository;
    private final SizeOptionRepository sizeOptionRepository;
    private final SizeChartMeasurementRepository measurementRepository;
    private final AuditService auditService;

    public SizeChartService(
            SizeChartRepository sizeChartRepository,
            SizeOptionRepository sizeOptionRepository,
            SizeChartMeasurementRepository measurementRepository,
            AuditService auditService
    ) {
        this.sizeChartRepository = sizeChartRepository;
        this.sizeOptionRepository = sizeOptionRepository;
        this.measurementRepository = measurementRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Optional<SizeChart> resolveApplicableChart(String brandId, String categoryId, String region, String productId) {
        if (productId != null && !productId.isBlank()) {
            List<SizeChart> productCharts = sizeChartRepository.findByProductIdAndStatusInOrderByVersionDesc(productId, AUTHORITATIVE_STATUSES);
            if (!productCharts.isEmpty()) {
                return Optional.of(productCharts.getFirst());
            }
        }
        if (region != null && !region.isBlank()) {
            List<SizeChart> regional = sizeChartRepository.findByBrandIdAndCategoryIdAndRegionAndProductIdIsNullAndStatusInOrderByVersionDesc(
                    brandId, categoryId, region.toUpperCase(), AUTHORITATIVE_STATUSES);
            if (!regional.isEmpty()) {
                return Optional.of(regional.getFirst());
            }
        }
        List<SizeChart> brandCategory = sizeChartRepository.findByBrandIdAndCategoryIdAndProductIdIsNullAndStatusInOrderByVersionDesc(
                brandId, categoryId, AUTHORITATIVE_STATUSES);
        return brandCategory.isEmpty() ? Optional.empty() : Optional.of(brandCategory.getFirst());
    }

    @Transactional(readOnly = true)
    public List<SizeChartResponse> getBrandCharts(String brandId) {
        return sizeChartRepository.findByBrandIdOrderByVersionDesc(brandId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SizeChartResponse> getProductCharts(String productId, String brandId, String categoryId, String region) {
        List<SizeChart> direct = sizeChartRepository.findByProductIdOrderByVersionDesc(productId);
        if (!direct.isEmpty()) {
            return direct.stream().map(this::toResponse).toList();
        }
        return resolveApplicableChart(brandId, categoryId, region, productId)
                .map(c -> List.of(toResponse(c)))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public SizeChartResponse getChartById(String chartId) {
        SizeChart chart = sizeChartRepository.findById(chartId)
                .orElseThrow(() -> new ResourceNotFoundException("SizeChart", chartId));
        return toResponse(chart);
    }

    @Transactional
    public SizeChartResponse createChart(CreateSizeChartRequest req, String actorId, String initialStatus) {
        String unit = UnitConverter.normalizeUnit(req.unit());
        String basis = req.measurementBasis().trim().toUpperCase();
        if (!"BODY".equals(basis) && !"GARMENT".equals(basis)) {
            throw new BadRequestException("INVALID_MEASUREMENT_BASIS", "measurementBasis must be BODY or GARMENT");
        }
        String region = req.region() != null && !req.region().isBlank() ? req.region().trim().toUpperCase() : "IN";
        int nextVersion = sizeChartRepository.findByBrandIdOrderByVersionDesc(req.brandId()).stream()
                .filter(c -> c.getCategoryId().equals(req.categoryId()) && c.getRegion().equalsIgnoreCase(region))
                .mapToInt(SizeChart::getVersion).max().orElse(0) + 1;

        OffsetDateTime now = OffsetDateTime.now();
        boolean verified = "ACTIVE".equalsIgnoreCase(initialStatus) || "VERIFIED".equalsIgnoreCase(initialStatus);
        if (verified && req.productId() == null) {
            supersedePreviousBrandCharts(req.brandId(), req.categoryId(), region);
        }

        SizeChart chart = new SizeChart(
                IdGenerator.newId("chart"), req.brandId(), req.productId(), req.categoryId(), region,
                req.chartName().trim(), basis, nextVersion, initialStatus,
                req.sourceType() != null ? req.sourceType() : "MANUAL_UPLOAD", req.sourceUrl(),
                req.extractionMethod() != null ? req.extractionMethod() : "MANUAL",
                req.confidence() != null ? req.confidence() : (verified ? new BigDecimal("0.98") : new BigDecimal("0.75")),
                now, verified ? now : null, verified ? actorId : null, now
        );
        sizeChartRepository.save(chart);

        int idx = 1;
        for (SizeOptionDto optDto : req.sizeOptions()) {
            SizeOption opt = new SizeOption(IdGenerator.newId("so"), chart.getId(), optDto.sizeLabel().trim().toUpperCase(), optDto.sortOrder() != null ? optDto.sortOrder() : idx++);
            sizeOptionRepository.save(opt);
            for (ChartDimensionRangeDto mDto : optDto.measurements()) {
                String dimCode = UnitConverter.normalizeDimensionCode(mDto.dimension());
                BigDecimal minCm = UnitConverter.toCentimeters(mDto.minValue(), unit);
                BigDecimal maxCm = mDto.maxValue() != null ? UnitConverter.toCentimeters(mDto.maxValue(), unit) : minCm;
                String itemBasis = mDto.measurementBasis() != null ? mDto.measurementBasis().trim().toUpperCase() : basis;
                measurementRepository.save(new SizeChartMeasurement(IdGenerator.newId("scm"), opt.getId(), dimCode, minCm, maxCm, unit, itemBasis));
            }
        }
        auditService.record(actorId, "SIZE_CHART_CREATED", "SIZE_CHART", chart.getId(), "{\"status\":\"" + initialStatus + "\"}");
        return toResponse(chart);
    }

    @Transactional
    public SizeChartResponse updateChartStatus(String chartId, String newStatus, String actorId) {
        SizeChart chart = sizeChartRepository.findById(chartId)
                .orElseThrow(() -> new ResourceNotFoundException("SizeChart", chartId));
        String statusUpper = newStatus.trim().toUpperCase();
        if ("ACTIVE".equals(statusUpper) && chart.getProductId() == null) {
            supersedePreviousBrandCharts(chart.getBrandId(), chart.getCategoryId(), chart.getRegion());
        }
        chart.setStatus(statusUpper);
        if ("VERIFIED".equals(statusUpper) || "ACTIVE".equals(statusUpper)) {
            chart.setVerifiedAt(OffsetDateTime.now());
            chart.setVerifiedBy(actorId);
            if (chart.getConfidence().compareTo(new BigDecimal("0.90")) < 0) {
                chart.setConfidence(new BigDecimal("0.95"));
            }
        }
        sizeChartRepository.save(chart);
        auditService.record(actorId, "SIZE_CHART_STATUS_" + statusUpper, "SIZE_CHART", chartId, null);
        return toResponse(chart);
    }

    private void supersedePreviousBrandCharts(String brandId, String categoryId, String region) {
        for (SizeChart c : sizeChartRepository.findByBrandIdAndCategoryIdAndRegionAndProductIdIsNullAndStatusInOrderByVersionDesc(brandId, categoryId, region, AUTHORITATIVE_STATUSES)) {
            c.setStatus("SUPERSEDED");
            sizeChartRepository.save(c);
        }
    }

    @Transactional(readOnly = true)
    public SizeChartResponse toResponse(SizeChart chart) {
        List<SizeOption> options = sizeOptionRepository.findBySizeChartIdOrderBySortOrderAsc(chart.getId());
        List<String> optionIds = options.stream().map(SizeOption::getId).toList();
        Map<String, List<SizeChartMeasurement>> byOption = optionIds.isEmpty() ? Map.of()
                : measurementRepository.findBySizeOptionIdIn(optionIds).stream().collect(Collectors.groupingBy(SizeChartMeasurement::getSizeOptionId));
        List<SizeOptionDto> optionDtos = new ArrayList<>();
        for (SizeOption opt : options) {
            List<ChartDimensionRangeDto> mDtos = byOption.getOrDefault(opt.getId(), List.of()).stream()
                    .map(m -> new ChartDimensionRangeDto(m.getDimensionCode(), m.getMinValueCm(), m.getMaxValueCm(), m.getMeasurementBasis()))
                    .toList();
            optionDtos.add(new SizeOptionDto(opt.getSizeLabel(), opt.getSortOrder(), mDtos));
        }
        return new SizeChartResponse(
                chart.getId(), chart.getBrandId(), chart.getProductId(), chart.getCategoryId(), chart.getRegion(),
                chart.getChartName(), chart.getMeasurementBasis(), chart.getVersion(), chart.getStatus(),
                chart.getSourceType(), chart.getSourceUrl(), chart.getExtractionMethod(), chart.getConfidence(),
                chart.getVerifiedAt(), chart.getVerifiedBy(), chart.getCreatedAt(), optionDtos
        );
    }
}


