package com.sizeok.recommendation.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sizeok.catalog.category.CategoryDimensionWeight;
import com.sizeok.catalog.category.CategoryDimensionWeightRepository;
import com.sizeok.catalog.product.Product;
import com.sizeok.catalog.product.ProductRepository;
import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.measurement.controller.MeasurementDtos.InferredDimensionRange;
import com.sizeok.measurement.domain.MeasurementSet;
import com.sizeok.measurement.domain.MeasurementValue;
import com.sizeok.measurement.domain.ReferenceSize;
import com.sizeok.measurement.repository.MeasurementSetRepository;
import com.sizeok.measurement.repository.MeasurementValueRepository;
import com.sizeok.measurement.repository.ReferenceSizeRepository;
import com.sizeok.measurement.service.MeasurementService;
import com.sizeok.profile.domain.Profile;
import com.sizeok.profile.service.ProfileService;
import com.sizeok.recommendation.controller.RecommendationDtos.CandidateSizeSummary;
import com.sizeok.recommendation.controller.RecommendationDtos.DimensionFitDetail;
import com.sizeok.recommendation.controller.RecommendationDtos.GenerateRecommendationRequest;
import com.sizeok.recommendation.controller.RecommendationDtos.RecommendationResponse;
import com.sizeok.recommendation.domain.Recommendation;
import com.sizeok.recommendation.domain.RecommendationCandidate;
import com.sizeok.recommendation.domain.RecommendationCandidateRepository;
import com.sizeok.recommendation.domain.RecommendationRepository;
import com.sizeok.sizechart.domain.SizeChart;
import com.sizeok.sizechart.domain.SizeChartMeasurement;
import com.sizeok.sizechart.domain.SizeOption;
import com.sizeok.sizechart.repository.SizeChartMeasurementRepository;
import com.sizeok.sizechart.repository.SizeOptionRepository;
import com.sizeok.sizechart.service.SizeChartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    private final ProfileService profileService;
    private final ProductRepository productRepository;
    private final CategoryDimensionWeightRepository weightRepository;
    private final MeasurementSetRepository measurementSetRepository;
    private final MeasurementValueRepository measurementValueRepository;
    private final ReferenceSizeRepository referenceSizeRepository;
    private final MeasurementService measurementService;
    private final SizeChartService sizeChartService;
    private final SizeOptionRepository sizeOptionRepository;
    private final SizeChartMeasurementRepository sizeChartMeasurementRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final String algorithmVersion;

    public RecommendationEngine(
            ProfileService profileService,
            ProductRepository productRepository,
            CategoryDimensionWeightRepository weightRepository,
            MeasurementSetRepository measurementSetRepository,
            MeasurementValueRepository measurementValueRepository,
            ReferenceSizeRepository referenceSizeRepository,
            MeasurementService measurementService,
            SizeChartService sizeChartService,
            SizeOptionRepository sizeOptionRepository,
            SizeChartMeasurementRepository sizeChartMeasurementRepository,
            RecommendationRepository recommendationRepository,
            RecommendationCandidateRepository candidateRepository,
            AuditService auditService,
            ObjectMapper objectMapper,
            @Value("${sizeok.recommendation.algorithm-version:1.0}") String algorithmVersion
    ) {
        this.profileService = profileService;
        this.productRepository = productRepository;
        this.weightRepository = weightRepository;
        this.measurementSetRepository = measurementSetRepository;
        this.measurementValueRepository = measurementValueRepository;
        this.referenceSizeRepository = referenceSizeRepository;
        this.measurementService = measurementService;
        this.sizeChartService = sizeChartService;
        this.sizeOptionRepository = sizeOptionRepository;
        this.sizeChartMeasurementRepository = sizeChartMeasurementRepository;
        this.recommendationRepository = recommendationRepository;
        this.candidateRepository = candidateRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.algorithmVersion = algorithmVersion;
    }

    @Transactional
    public RecommendationResponse generateRecommendation(GenerateRecommendationRequest request) {
        Profile profile = profileService.requireAccessibleProfile(request.profileId());
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        String fitPref = request.fitPreference() != null && !request.fitPreference().isBlank()
                ? request.fitPreference().trim().toUpperCase()
                : profile.getDefaultFitPreference();

        SizeChart chart = sizeChartService.resolveApplicableChart(
                        product.getBrandId(), product.getCategoryId(), product.getRegion(), product.getId())
                .orElseThrow(() -> new BadRequestException("CHART_NOT_AVAILABLE",
                        "No verified size chart available for product " + product.getId()));

        // Stage 1: Load normalized user measurements & category-specific reference evidence
        Optional<MeasurementSet> activeSetOpt = measurementSetRepository.findFirstByProfileIdAndStatusOrderByVersionDesc(profile.getId(), "ACTIVE");
        Map<String, Double> bodyMeasurementsCm = new LinkedHashMap<>();
        boolean hasActualBodyMeasurements = false;
        if (activeSetOpt.isPresent()) {
            for (MeasurementValue mv : measurementValueRepository.findByMeasurementSetIdOrderByDimensionCodeAsc(activeSetOpt.get().getId())) {
                bodyMeasurementsCm.put(mv.getDimensionCode(), mv.getValueCm().doubleValue());
                hasActualBodyMeasurements = true;
            }
        }

        List<ReferenceSize> categoryRefs = referenceSizeRepository.findByProfileIdAndCategoryIdOrderByCreatedAtDesc(
                profile.getId(), product.getCategoryId());
        List<String> refConflicts = measurementService.detectCategoryReferenceConflicts(categoryRefs);

        Map<String, Double> effectiveMeasurementsCm = new LinkedHashMap<>(bodyMeasurementsCm);
        for (ReferenceSize ref : categoryRefs) {
            Map<String, InferredDimensionRange> ranges = measurementService.readInferredRanges(ref.getInferredRangesJson());
            for (Map.Entry<String, InferredDimensionRange> e : ranges.entrySet()) {
                effectiveMeasurementsCm.putIfAbsent(e.getKey(),
                        e.getValue().minCm().add(e.getValue().maxCm()).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP).doubleValue());
            }
        }

        if (effectiveMeasurementsCm.isEmpty()) {
            throw new BadRequestException("INSUFFICIENT_MEASUREMENT_EVIDENCE",
                    "Profile has neither body measurements nor category-matching reference sizes for " + product.getCategoryId());
        }

        return evaluateAndSave(profile, product, chart, activeSetOpt.orElse(null), fitPref,
                effectiveMeasurementsCm, hasActualBodyMeasurements, categoryRefs.size(), !refConflicts.isEmpty(), refConflicts);
    }

    private RecommendationResponse evaluateAndSave(
            Profile profile, Product product, SizeChart chart, MeasurementSet measurementSet,
            String fitPreference, Map<String, Double> userCmMap, boolean hasActualBodyMeasurements,
            int categoryRefCount, boolean hasConflict, List<String> conflictWarnings
    ) {
        List<CategoryDimensionWeight> weightConfigs = weightRepository.findByCategoryId(product.getCategoryId());
        Map<String, CategoryDimensionWeight> weightByDim = weightConfigs.stream()
                .collect(Collectors.toMap(CategoryDimensionWeight::getDimensionCode, w -> w));
        List<SizeOption> options = sizeOptionRepository.findBySizeChartIdOrderBySortOrderAsc(chart.getId());
        if (options.isEmpty()) {
            throw new BadRequestException("EMPTY_SIZE_CHART", "Size chart has no size options: " + chart.getId());
        }
        Map<String, List<SizeChartMeasurement>> byOption = sizeChartMeasurementRepository
                .findBySizeOptionIdIn(options.stream().map(SizeOption::getId).toList())
                .stream().collect(Collectors.groupingBy(SizeChartMeasurement::getSizeOptionId));

        double stretchFactor = "HIGH".equalsIgnoreCase(product.getStretchLevel()) ? 0.65
                : ("MEDIUM".equalsIgnoreCase(product.getStretchLevel()) ? 0.80 : ("NONE".equalsIgnoreCase(product.getStretchLevel()) ? 1.00 : 0.90));

        record ScoredCandidate(String sizeLabel, int fitScore, Map<String, DimensionFitDetail> analysis, List<String> explanations) {}
        List<ScoredCandidate> scored = new ArrayList<>();

        for (SizeOption opt : options) {
            double weightedPenalty = 0.0;
            double weightSum = 0.0;
            Map<String, DimensionFitDetail> dimDetails = new LinkedHashMap<>();
            List<String> sizeExplanations = new ArrayList<>();

            for (SizeChartMeasurement scm : byOption.getOrDefault(opt.getId(), List.of())) {
                String dim = scm.getDimensionCode();
                Double userVal = userCmMap.get(dim);
                if (userVal == null) continue;
                CategoryDimensionWeight cfg = weightByDim.get(dim);
                double w = cfg != null ? cfg.getWeight().doubleValue() : 0.15;
                weightSum += w;

                double tMin = scm.getMinValueCm().doubleValue();
                double tMax = scm.getMaxValueCm().doubleValue();
                if ("GARMENT".equalsIgnoreCase(scm.getMeasurementBasis()) || "GARMENT".equalsIgnoreCase(chart.getMeasurementBasis())) {
                    double ease = (cfg != null ? cfg.getEaseForFitPreference(fitPreference).doubleValue() : 6.0) * stretchFactor;
                    tMin -= (ease + 2.0);
                    tMax -= (ease - 2.0);
                }

                double penalty = 0.0;
                String status = "GOOD_FIT";
                String contrib = "Within recommended range";
                if (userVal < tMin) {
                    double diff = tMin - userVal;
                    penalty = Math.min(100.0, diff * 11.0);
                    status = diff > 3.0 ? "LOOSE" : "SLIGHTLY_LOOSE";
                    contrib = "Below target range";
                } else if (userVal > tMax) {
                    double diff = userVal - tMax;
                    penalty = Math.min(100.0, diff * 14.0);
                    status = diff > 2.5 ? "TIGHT" : "SLIGHTLY_TIGHT";
                    contrib = "Exceeds target range";
                } else {
                    sizeExplanations.add("Your " + dim.toLowerCase() + " measurement matches size " + opt.getSizeLabel());
                }
                weightedPenalty += w * penalty;
                dimDetails.put(dim.toLowerCase(), new DimensionFitDetail(status, contrib, round1(userVal), round1(tMin), round1(tMax), round1(penalty)));
            }
            int score = (int) Math.round(Math.max(0.0, Math.min(100.0, 100.0 - (weightSum > 0 ? weightedPenalty / weightSum : 50.0))));
            scored.add(new ScoredCandidate(opt.getSizeLabel(), score, dimDetails, sizeExplanations));
        }

        int matchedPrimary = countMatchedPrimary(weightConfigs, userCmMap, options, byOption);
        int totalPrimary = (int) weightConfigs.stream().filter(CategoryDimensionWeight::isPrimaryDimension).count();
        scored.sort(Comparator.comparingInt(ScoredCandidate::fitScore).reversed());
        ScoredCandidate best = scored.getFirst();
        ScoredCandidate alt = scored.size() > 1 ? scored.get(1) : null;
        String confidence = calculateConfidence(hasActualBodyMeasurements, chart, matchedPrimary, totalPrimary, categoryRefCount, hasConflict);

        List<String> expl = new ArrayList<>(best.explanations());
        if (alt != null) expl.add("Size " + alt.sizeLabel() + " is an alternative (score " + alt.fitScore() + ")");
        expl.addAll(conflictWarnings);
        if ("GARMENT".equalsIgnoreCase(chart.getMeasurementBasis())) {
            expl.add("Garment ease applied for " + fitPreference.toLowerCase() + " fit preference");
        }
        return persistRecommendation(profile, product, chart, measurementSet, fitPreference, best.sizeLabel(),
                alt != null ? alt.sizeLabel() : null, best.fitScore(), confidence, best.analysis(), expl, userCmMap,
                scored.stream().map(s -> new CandidateSizeSummary(s.sizeLabel(), s.fitScore(), 0)).toList());
    }

    private int countMatchedPrimary(
            List<CategoryDimensionWeight> weightConfigs,
            Map<String, Double> userCmMap,
            List<SizeOption> options,
            Map<String, List<SizeChartMeasurement>> byOption
    ) {
        int matched = 0;
        for (CategoryDimensionWeight cfg : weightConfigs) {
            if (cfg.isPrimaryDimension() && userCmMap.containsKey(cfg.getDimensionCode())) {
                boolean inChart = options.stream()
                        .flatMap(o -> byOption.getOrDefault(o.getId(), List.of()).stream())
                        .anyMatch(m -> m.getDimensionCode().equals(cfg.getDimensionCode()));
                if (inChart) matched++;
            }
        }
        return matched;
    }

    private String calculateConfidence(
            boolean hasActualBodyMeasurements,
            SizeChart chart,
            int matchedPrimary,
            int totalPrimary,
            int categoryRefCount,
            boolean hasConflict
    ) {
        double ev = 0.0;
        if (hasActualBodyMeasurements) ev += 0.45;
        else if (categoryRefCount > 0) ev += 0.25;

        if (chart.isVerifiedOrActive()) ev += 0.30;
        else ev += 0.10;

        if (totalPrimary > 0) {
            double coverage = (double) matchedPrimary / totalPrimary;
            ev += 0.25 * coverage;
            if (coverage < 0.99) {
                ev -= 0.20;
            }
        } else {
            ev += 0.15;
        }

        if (hasConflict) {
            ev -= 0.30;
        }
        if (ev >= 0.80) return "HIGH";
        if (ev >= 0.50) return "MEDIUM";
        return "LOW";
    }

    private RecommendationResponse persistRecommendation(
            Profile profile, Product product, SizeChart chart, MeasurementSet measurementSet,
            String fitPreference, String recommendedSize, String alternativeSize, int fitScore,
            String confidence, Map<String, DimensionFitDetail> analysis, List<String> explanations,
            Map<String, Double> snapshotMeasurements, List<CandidateSizeSummary> rawCandidates
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        String fitSummary = ("LOW".equals(confidence) ? "Low-confidence estimate; " : "")
                + fitPreference.substring(0, 1).toUpperCase() + fitPreference.substring(1).toLowerCase() + " fit expected";

        Recommendation rec = new Recommendation(
                IdGenerator.newId("rec"),
                profile.getId(),
                product.getId(),
                measurementSet != null ? measurementSet.getId() : null,
                measurementSet != null ? measurementSet.getVersion() : null,
                chart.getId(),
                chart.getVersion(),
                fitPreference,
                recommendedSize,
                alternativeSize,
                fitScore,
                confidence,
                fitSummary,
                writeJson(analysis),
                writeJson(explanations),
                writeJson(snapshotMeasurements),
                algorithmVersion,
                now
        );
        recommendationRepository.save(rec);

        List<CandidateSizeSummary> ranked = new ArrayList<>();
        int rank = 1;
        for (CandidateSizeSummary c : rawCandidates) {
            candidateRepository.save(new RecommendationCandidate(
                    IdGenerator.newId("rc"), rec.getId(), c.sizeLabel(), c.fitScore(), rank, null
            ));
            ranked.add(new CandidateSizeSummary(c.sizeLabel(), c.fitScore(), rank++));
        }
        auditService.record(profile.getUserId(), "RECOMMENDATION_GENERATED", "RECOMMENDATION", rec.getId(), null);
        return toResponse(rec, ranked);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendation(String id) {
        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", id));
        profileService.requireAccessibleProfile(rec.getProfileId());
        List<CandidateSizeSummary> cands = candidateRepository.findByRecommendationIdOrderByRankOrderAsc(id).stream()
                .map(c -> new CandidateSizeSummary(c.getSizeLabel(), c.getFitScore(), c.getRankOrder()))
                .toList();
        return toResponse(rec, cands);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationForAudit(String id) {
        Recommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", id));
        List<CandidateSizeSummary> cands = candidateRepository.findByRecommendationIdOrderByRankOrderAsc(id).stream()
                .map(c -> new CandidateSizeSummary(c.getSizeLabel(), c.getFitScore(), c.getRankOrder()))
                .toList();
        return toResponse(rec, cands);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getProfileRecommendations(String profileId) {
        profileService.requireAccessibleProfile(profileId);
        return recommendationRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                .map(r -> {
                    List<CandidateSizeSummary> cands = candidateRepository.findByRecommendationIdOrderByRankOrderAsc(r.getId()).stream()
                            .map(c -> new CandidateSizeSummary(c.getSizeLabel(), c.getFitScore(), c.getRankOrder()))
                            .toList();
                    return toResponse(r, cands);
                })
                .toList();
    }

    private RecommendationResponse toResponse(Recommendation r, List<CandidateSizeSummary> candidates) {
        Map<String, DimensionFitDetail> analysis = readJson(r.getDimensionAnalysisJson(), new TypeReference<>() {}, Map.of());
        List<String> explanations = readJson(r.getExplanationJson(), new TypeReference<>() {}, List.of());
        return new RecommendationResponse(
                r.getId(), r.getProfileId(), r.getProductId(), r.getMeasurementSetVersion(),
                r.getSizeChartId(), r.getChartVersion(), r.getFitPreference(), r.getRecommendedSize(),
                r.getAlternativeSize(), r.getFitScore(), r.getConfidence(), r.getFitSummary(),
                analysis, explanations, candidates, r.getAlgorithmVersion(), r.getCreatedAt()
        );
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private <T> T readJson(String json, TypeReference<T> typeRef, T fallback) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return fallback;
        }
    }

    private double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}




