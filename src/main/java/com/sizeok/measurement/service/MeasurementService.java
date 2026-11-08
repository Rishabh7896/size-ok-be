package com.sizeok.measurement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.common.util.UnitConverter;
import com.sizeok.measurement.controller.MeasurementDtos.AddReferenceSizeRequest;
import com.sizeok.measurement.controller.MeasurementDtos.CreateMeasurementSetRequest;
import com.sizeok.measurement.controller.MeasurementDtos.InferredDimensionRange;
import com.sizeok.measurement.controller.MeasurementDtos.MeasurementDimensionInput;
import com.sizeok.measurement.controller.MeasurementDtos.MeasurementSetResponse;
import com.sizeok.measurement.controller.MeasurementDtos.MeasurementValueDto;
import com.sizeok.measurement.controller.MeasurementDtos.PatchMeasurementSetRequest;
import com.sizeok.measurement.controller.MeasurementDtos.ReferenceSizeResponse;
import com.sizeok.measurement.domain.MeasurementSet;
import com.sizeok.measurement.domain.MeasurementValue;
import com.sizeok.measurement.domain.ReferenceSize;
import com.sizeok.measurement.repository.MeasurementSetRepository;
import com.sizeok.measurement.repository.MeasurementValueRepository;
import com.sizeok.measurement.repository.ReferenceSizeRepository;
import com.sizeok.profile.domain.Profile;
import com.sizeok.profile.service.ProfileService;
import com.sizeok.sizechart.domain.SizeChart;
import com.sizeok.sizechart.domain.SizeChartMeasurement;
import com.sizeok.sizechart.domain.SizeOption;
import com.sizeok.sizechart.repository.SizeChartMeasurementRepository;
import com.sizeok.sizechart.repository.SizeOptionRepository;
import com.sizeok.sizechart.service.SizeChartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MeasurementService {

    private final MeasurementSetRepository measurementSetRepository;
    private final MeasurementValueRepository measurementValueRepository;
    private final ReferenceSizeRepository referenceSizeRepository;
    private final ProfileService profileService;
    private final SizeChartService sizeChartService;
    private final SizeOptionRepository sizeOptionRepository;
    private final SizeChartMeasurementRepository sizeChartMeasurementRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public MeasurementService(
            MeasurementSetRepository measurementSetRepository,
            MeasurementValueRepository measurementValueRepository,
            ReferenceSizeRepository referenceSizeRepository,
            ProfileService profileService,
            SizeChartService sizeChartService,
            SizeOptionRepository sizeOptionRepository,
            SizeChartMeasurementRepository sizeChartMeasurementRepository,
            AuditService auditService,
            ObjectMapper objectMapper
    ) {
        this.measurementSetRepository = measurementSetRepository;
        this.measurementValueRepository = measurementValueRepository;
        this.referenceSizeRepository = referenceSizeRepository;
        this.profileService = profileService;
        this.sizeChartService = sizeChartService;
        this.sizeOptionRepository = sizeOptionRepository;
        this.sizeChartMeasurementRepository = sizeChartMeasurementRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MeasurementSetResponse saveMeasurementSet(String profileId, CreateMeasurementSetRequest request) {
        Profile profile = profileService.requireAccessibleProfile(profileId);
        String unit = UnitConverter.normalizeUnit(request.unit() != null ? request.unit() : profile.getPreferredUnit());
        String source = normalizeBodyMeasurementSource(request.source());

        List<MeasurementSet> existingSets = measurementSetRepository.findByProfileIdOrderByVersionDesc(profileId);
        int nextVersion = existingSets.isEmpty() ? 1 : existingSets.getFirst().getVersion() + 1;
        for (MeasurementSet prev : existingSets) {
            if ("ACTIVE".equalsIgnoreCase(prev.getStatus())) {
                prev.setStatus("SUPERSEDED");
                measurementSetRepository.save(prev);
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        MeasurementSet set = new MeasurementSet(IdGenerator.newId("ms"), profileId, nextVersion, unit, source, "ACTIVE", now, request.notes(), now);
        measurementSetRepository.save(set);

        for (MeasurementDimensionInput input : request.measurements()) {
            String dimCode = UnitConverter.normalizeDimensionCode(input.dimension());
            BigDecimal valueCm = UnitConverter.toCentimeters(input.value(), unit);
            UnitConverter.validateDimensionRangeCm(dimCode, valueCm);
            String dimSource = input.source() != null ? normalizeBodyMeasurementSource(input.source()) : source;
            BigDecimal confidence = input.confidence() != null ? input.confidence() : ("MEASURED".equals(dimSource) ? new BigDecimal("0.98") : new BigDecimal("0.85"));
            measurementValueRepository.save(new MeasurementValue(IdGenerator.newId("mv"), set.getId(), dimCode, valueCm, input.value(), unit, dimSource, confidence, now));
        }
        auditService.record(profile.getUserId(), "MEASUREMENT_SET_CREATED", "MEASUREMENT_SET", set.getId(), "{\"version\":" + nextVersion + "}");
        return toMeasurementSetResponse(set);
    }

    @Transactional(readOnly = true)
    public List<MeasurementSetResponse> getMeasurementHistory(String profileId) {
        profileService.requireAccessibleProfile(profileId);
        return measurementSetRepository.findByProfileIdOrderByVersionDesc(profileId).stream()
                .map(this::toMeasurementSetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeasurementSetResponse getMeasurementSet(String measurementSetId) {
        MeasurementSet set = measurementSetRepository.findById(measurementSetId)
                .orElseThrow(() -> new ResourceNotFoundException("MeasurementSet", measurementSetId));
        profileService.requireAccessibleProfile(set.getProfileId());
        return toMeasurementSetResponse(set);
    }

    /**
     * Corrects/updates measurements by creating a new immutable MeasurementSet version
     * and marking the previous version SUPERSEDED.
     */
    @Transactional
    public MeasurementSetResponse patchMeasurementSet(String measurementSetId, PatchMeasurementSetRequest request) {
        MeasurementSet baseSet = measurementSetRepository.findById(measurementSetId)
                .orElseThrow(() -> new ResourceNotFoundException("MeasurementSet", measurementSetId));
        Profile profile = profileService.requireAccessibleProfile(baseSet.getProfileId());

        String unit = UnitConverter.normalizeUnit(request.unit() != null ? request.unit() : baseSet.getOriginalUnit());
        String source = request.source() != null ? normalizeBodyMeasurementSource(request.source()) : baseSet.getSource();

        Map<String, MeasurementDimensionInput> mergedInputs = new LinkedHashMap<>();
        for (MeasurementValue mv : measurementValueRepository.findByMeasurementSetIdOrderByDimensionCodeAsc(baseSet.getId())) {
            BigDecimal convertedVal = UnitConverter.fromCentimeters(mv.getValueCm(), unit);
            mergedInputs.put(mv.getDimensionCode(), new MeasurementDimensionInput(mv.getDimensionCode(), convertedVal, mv.getSource(), mv.getConfidence()));
        }
        for (MeasurementDimensionInput patchInput : request.measurements()) {
            String dimCode = UnitConverter.normalizeDimensionCode(patchInput.dimension());
            mergedInputs.put(dimCode, new MeasurementDimensionInput(dimCode, patchInput.value(), patchInput.source(), patchInput.confidence()));
        }

        return saveMeasurementSet(profile.getId(), new CreateMeasurementSetRequest(
                unit,
                source,
                request.notes() != null ? request.notes() : "Updated from version " + baseSet.getVersion(),
                new ArrayList<>(mergedInputs.values())
        ));
    }

    private String normalizeBodyMeasurementSource(String source) {
        if (source == null || source.isBlank()) {
            return "MEASURED";
        }
        String upper = source.trim().toUpperCase();
        if ("REFERENCE_INFERRED".equals(upper)) {
            throw new BadRequestException(
                    "INVALID_MEASUREMENT_SOURCE",
                    "A measurement inferred from a brand size must not be stored as an actual measured body dimension."
            );
        }
        return upper;
    }

    @Transactional
    public ReferenceSizeResponse addReferenceSize(String profileId, AddReferenceSizeRequest request) {
        Profile profile = profileService.requireAccessibleProfile(profileId);
        String region = request.region() != null && !request.region().isBlank() ? request.region().trim().toUpperCase() : profile.getRegion();
        String fitPref = request.fitPreference() != null && !request.fitPreference().isBlank()
                ? request.fitPreference().trim().toUpperCase() : profile.getDefaultFitPreference();
        String sizeLabel = request.sizeLabel().trim().toUpperCase();

        SizeChart chart = sizeChartService.resolveApplicableChart(request.brandId(), request.categoryId(), region, request.productId())
                .orElseThrow(() -> new BadRequestException("CHART_NOT_FOUND",
                        "No active size chart found for brand=" + request.brandId() + ", category=" + request.categoryId()));

        SizeOption option = sizeOptionRepository.findBySizeChartIdAndSizeLabelIgnoreCase(chart.getId(), sizeLabel)
                .orElseThrow(() -> new BadRequestException("SIZE_LABEL_NOT_FOUND",
                        "Size label '" + sizeLabel + "' not found in chart " + chart.getChartName()));

        List<SizeChartMeasurement> chartMeasurements = sizeChartMeasurementRepository.findBySizeOptionId(option.getId());
        Map<String, InferredDimensionRange> inferred = new LinkedHashMap<>();
        for (SizeChartMeasurement m : chartMeasurements) {
            inferred.put(m.getDimensionCode(), new InferredDimensionRange(m.getMinValueCm(), m.getMaxValueCm(), m.getMeasurementBasis()));
        }

        String json = writeJson(inferred);
        ReferenceSize ref = new ReferenceSize(
                IdGenerator.newId("ref"), profileId, request.brandId(), request.categoryId(),
                request.productId(), chart.getId(), sizeLabel, region, fitPref, json, OffsetDateTime.now()
        );
        referenceSizeRepository.save(ref);
        auditService.record(profile.getUserId(), "REFERENCE_SIZE_ADDED", "REFERENCE_SIZE", ref.getId(), null);

        List<ReferenceSize> categoryRefs = referenceSizeRepository.findByProfileIdAndCategoryIdOrderByCreatedAtDesc(profileId, request.categoryId());
        return toReferenceSizeResponse(ref, categoryRefs);
    }

    @Transactional(readOnly = true)
    public List<ReferenceSizeResponse> getReferenceSizes(String profileId) {
        profileService.requireAccessibleProfile(profileId);
        List<ReferenceSize> allRefs = referenceSizeRepository.findByProfileIdOrderByCreatedAtDesc(profileId);
        return allRefs.stream()
                .map(r -> toReferenceSizeResponse(r, allRefs.stream().filter(o -> o.getCategoryId().equals(r.getCategoryId())).toList()))
                .toList();
    }

    @Transactional
    public void deleteReferenceSize(String referenceSizeId) {
        ReferenceSize ref = referenceSizeRepository.findById(referenceSizeId)
                .orElseThrow(() -> new ResourceNotFoundException("ReferenceSize", referenceSizeId));
        Profile profile = profileService.requireAccessibleProfile(ref.getProfileId());
        referenceSizeRepository.delete(ref);
        auditService.record(profile.getUserId(), "REFERENCE_SIZE_DELETED", "REFERENCE_SIZE", referenceSizeId, null);
    }

    public List<String> detectCategoryReferenceConflicts(List<ReferenceSize> sameCategoryRefs) {
        List<String> warnings = new ArrayList<>();
        if (sameCategoryRefs.size() < 2) {
            return warnings;
        }
        Map<String, List<InferredDimensionRange>> byDim = new LinkedHashMap<>();
        for (ReferenceSize ref : sameCategoryRefs) {
            Map<String, InferredDimensionRange> map = readInferredRanges(ref.getInferredRangesJson());
            for (Map.Entry<String, InferredDimensionRange> entry : map.entrySet()) {
                byDim.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }
        for (Map.Entry<String, List<InferredDimensionRange>> entry : byDim.entrySet()) {
            List<InferredDimensionRange> ranges = entry.getValue();
            if (ranges.size() >= 2) {
                BigDecimal maxOfMins = ranges.stream().map(InferredDimensionRange::minCm).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal minOfMaxs = ranges.stream().map(InferredDimensionRange::maxCm).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                if (maxOfMins.subtract(minOfMaxs).compareTo(new BigDecimal("2.00")) > 0) {
                    warnings.add("Conflicting reference size evidence on dimension " + entry.getKey()
                            + " (disjoint ranges across reference brands)");
                }
            }
        }
        return warnings;
    }

    public Map<String, InferredDimensionRange> readInferredRanges(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    public MeasurementSetResponse toMeasurementSetResponse(MeasurementSet set) {
        List<MeasurementValue> values = measurementValueRepository.findByMeasurementSetIdOrderByDimensionCodeAsc(set.getId());
        Map<String, BigDecimal> mapCm = new LinkedHashMap<>();
        List<MeasurementValueDto> detailed = new ArrayList<>();
        for (MeasurementValue v : values) {
            mapCm.put(v.getDimensionCode(), v.getValueCm());
            detailed.add(new MeasurementValueDto(v.getDimensionCode(), v.getValueCm(), v.getOriginalValue(), v.getOriginalUnit(), v.getSource(), v.getConfidence()));
        }
        return new MeasurementSetResponse(
                set.getId(), set.getProfileId(), set.getVersion(), set.getOriginalUnit(),
                set.getSource(), set.getStatus(), set.getMeasuredAt(), set.getCreatedAt(), mapCm, detailed
        );
    }

    private ReferenceSizeResponse toReferenceSizeResponse(ReferenceSize ref, List<ReferenceSize> sameCategoryRefs) {
        Map<String, InferredDimensionRange> inferred = readInferredRanges(ref.getInferredRangesJson());
        List<String> conflicts = detectCategoryReferenceConflicts(sameCategoryRefs);
        return new ReferenceSizeResponse(
                ref.getId(), ref.getProfileId(), ref.getBrandId(), ref.getCategoryId(),
                ref.getProductId(), ref.getSizeChartId(), ref.getSizeLabel(), ref.getRegion(),
                ref.getFitPreference(), inferred, !conflicts.isEmpty(), conflicts, ref.getCreatedAt()
        );
    }
}



