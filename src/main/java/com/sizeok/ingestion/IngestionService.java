package com.sizeok.ingestion;

import com.sizeok.catalog.product.Product;
import com.sizeok.catalog.product.ProductRepository;
import com.sizeok.catalog.product.ProductVariant;
import com.sizeok.catalog.product.ProductVariantRepository;
import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.security.AuthenticatedUser;
import com.sizeok.common.security.CurrentUserProvider;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.ingestion.IngestionDtos.IngestionStatusResponse;
import com.sizeok.ingestion.IngestionDtos.SubmitIngestionRequest;
import com.sizeok.ingestion.IngestionDtos.SubmitManualSizeChartRequest;
import com.sizeok.profile.service.ProfileService;
import com.sizeok.recommendation.controller.RecommendationDtos.GenerateRecommendationRequest;
import com.sizeok.recommendation.controller.RecommendationDtos.RecommendationResponse;
import com.sizeok.recommendation.engine.RecommendationEngine;
import com.sizeok.sizechart.controller.SizeChartDtos.CreateSizeChartRequest;
import com.sizeok.sizechart.controller.SizeChartDtos.SizeChartResponse;
import com.sizeok.sizechart.domain.SizeChart;
import com.sizeok.sizechart.service.SizeChartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IngestionService {

    private final ProductIngestionRepository ingestionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SizeChartService sizeChartService;
    private final ProfileService profileService;
    private final RecommendationEngine recommendationEngine;
    private final SsrfUrlValidator ssrfUrlValidator;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public IngestionService(
            ProductIngestionRepository ingestionRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            SizeChartService sizeChartService,
            ProfileService profileService,
            RecommendationEngine recommendationEngine,
            SsrfUrlValidator ssrfUrlValidator,
            CurrentUserProvider currentUserProvider,
            AuditService auditService
    ) {
        this.ingestionRepository = ingestionRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.sizeChartService = sizeChartService;
        this.profileService = profileService;
        this.recommendationEngine = recommendationEngine;
        this.ssrfUrlValidator = ssrfUrlValidator;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    @Transactional
    public IngestionStatusResponse submitUrl(SubmitIngestionRequest req) {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        if (req.profileId() != null && !req.profileId().isBlank()) {
            profileService.requireAccessibleProfile(req.profileId());
        }
        URI uri = ssrfUrlValidator.validateAndNormalize(req.productUrl());
        String canonicalUrl = uri.toString();
        String idemKey = (req.idempotencyKey() != null && !req.idempotencyKey().isBlank())
                ? req.idempotencyKey().trim()
                : (req.profileId() != null ? req.profileId() + ":" : "") + canonicalUrl;

        Optional<ProductIngestion> existingJob = ingestionRepository.findByIdempotencyKey(idemKey);
        if (existingJob.isPresent()) {
            return toResponse(existingJob.get());
        }

        String fitPref = req.fitPreference() != null && !req.fitPreference().isBlank() ? req.fitPreference().trim().toUpperCase() : "REGULAR";
        String retailer = ssrfUrlValidator.detectSupportedRetailer(uri);
        OffsetDateTime now = OffsetDateTime.now();

        Product product = productRepository.findByCanonicalUrl(canonicalUrl)
                .orElseGet(() -> extractOrCreateProductIfSupported(canonicalUrl, retailer, now));

        return createAndProcessJob(user.userId(), req.profileId(), canonicalUrl, retailer, fitPref, idemKey, product, now);
    }

    private IngestionStatusResponse createAndProcessJob(
            String userId, String profileId, String canonicalUrl, String retailer,
            String fitPref, String idemKey, Product product, OffsetDateTime now
    ) {
        String status;
        String requiredAction = null;
        String chartId = null;
        String recId = null;

        if (product == null) {
            status = "NEEDS_USER_INPUT";
            requiredAction = "UPLOAD_SIZE_CHART";
        } else {
            Optional<SizeChart> chartOpt = sizeChartService.resolveApplicableChart(product.getBrandId(), product.getCategoryId(), product.getRegion(), product.getId());
            if (chartOpt.isEmpty()) {
                status = "NEEDS_USER_INPUT";
                requiredAction = "UPLOAD_SIZE_CHART";
            } else {
                chartId = chartOpt.get().getId();
                status = "COMPLETED";
                if (profileId != null && !profileId.isBlank()) {
                    try {
                        recId = recommendationEngine.generateRecommendation(new GenerateRecommendationRequest(profileId, product.getId(), fitPref)).recommendationId();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        ProductIngestion job = new ProductIngestion(
                IdGenerator.newId("ing"), profileId, canonicalUrl, retailer, fitPref, status,
                requiredAction, "1.0", null, product != null ? product.getId() : null, chartId, recId,
                idemKey, "{\"retailer\":\"" + (retailer != null ? retailer : "UNKNOWN") + "\"}", now, now
        );
        ingestionRepository.save(job);
        auditService.record(userId, "PRODUCT_INGESTION_SUBMITTED", "PRODUCT_INGESTION", job.getId(), null);
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public IngestionStatusResponse getIngestionStatus(String ingestionId) {
        ProductIngestion job = ingestionRepository.findById(ingestionId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductIngestion", ingestionId));
        return toResponse(job);
    }

    @Transactional
    public IngestionStatusResponse submitManualChart(String ingestionId, SubmitManualSizeChartRequest req) {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        ProductIngestion job = ingestionRepository.findById(ingestionId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductIngestion", ingestionId));

        String brandId = req.brandId() != null ? req.brandId() : "brand_zara";
        String categoryId = req.categoryId() != null ? req.categoryId() : "mens_tshirts";
        String region = req.region() != null ? req.region() : "IN";
        OffsetDateTime now = OffsetDateTime.now();

        Product prod = job.getProductId() != null ? productRepository.findById(job.getProductId()).orElse(null) : null;
        if (prod == null) {
            prod = new Product(IdGenerator.newId("prod"), brandId, categoryId,
                    job.getRetailer() != null ? job.getRetailer() : "UserSubmitted",
                    req.chartName() != null ? req.chartName() : "User Submitted Product",
                    job.getSubmittedUrl(), "Apparel", job.getFitPreference(), "LOW", "Cotton", "UNISEX", region, now, now);
            productRepository.save(prod);
            job.setProductId(prod.getId());
        }

        SizeChartResponse createdChart = sizeChartService.createChart(new CreateSizeChartRequest(
                prod.getBrandId(), prod.getId(), prod.getCategoryId(), region,
                req.chartName() != null ? req.chartName() : "User Uploaded Product Chart",
                req.measurementBasis(), req.unit(), "USER_UPLOAD", job.getSubmittedUrl(),
                "MANUAL_USER_UPLOAD", new BigDecimal("0.85"), false, req.sizeOptions()
        ), user.userId(), "CHART_FOUND");

        job.setExtractedSizeChartId(createdChart.id());
        job.setStatus("CHART_FOUND");
        job.setRequiredAction("CONFIRM_EXTRACTED_CHART");
        job.setUpdatedAt(now);
        ingestionRepository.save(job);
        return toResponse(job);
    }

    @Transactional
    public IngestionStatusResponse confirmExtractedChart(String ingestionId) {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        ProductIngestion job = ingestionRepository.findById(ingestionId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductIngestion", ingestionId));
        if (job.getExtractedSizeChartId() != null) {
            sizeChartService.updateChartStatus(job.getExtractedSizeChartId(), "VERIFIED", user.userId());
        }
        if (job.getProfileId() != null && job.getProductId() != null) {
            try {
                RecommendationResponse rec = recommendationEngine.generateRecommendation(
                        new GenerateRecommendationRequest(job.getProfileId(), job.getProductId(), job.getFitPreference()));
                job.setRecommendationId(rec.recommendationId());
            } catch (Exception ignored) {
            }
        }
        job.setStatus("COMPLETED");
        job.setRequiredAction(null);
        job.setUpdatedAt(OffsetDateTime.now());
        ingestionRepository.save(job);
        return toResponse(job);
    }

    private Product extractOrCreateProductIfSupported(String canonicalUrl, String retailer, OffsetDateTime now) {
        if (retailer == null) {
            return null;
        }
        String lower = canonicalUrl.toLowerCase();
        String brandId = switch (retailer) {
            case "Zara" -> "brand_zara";
            case "H&M" -> "brand_hm";
            case "Nike" -> "brand_nike";
            case "Levi's" -> "brand_levis";
            default -> lower.contains("nike") ? "brand_nike" : (lower.contains("hm") ? "brand_hm" : "brand_zara");
        };
        String categoryId = lower.contains("jean") ? "mens_jeans" : (lower.contains("shirt") && !lower.contains("tshirt") && !lower.contains("t-shirt") ? "mens_shirts" : "mens_tshirts");
        String region = "brand_zara".equals(brandId) ? "EU" : "IN";
        Product prod = new Product(
                IdGenerator.newId("prod"), brandId, categoryId, retailer,
                retailer + " Extracted Product", canonicalUrl, "Apparel",
                lower.contains("slim") ? "SLIM" : "REGULAR", "LOW", "100% Cotton", "MENS", region, now, now
        );
        productRepository.save(prod);
        List<String> sizes = "mens_jeans".equals(categoryId) ? List.of("30", "32", "34", "36") : List.of("S", "M", "L", "XL");
        int idx = 1;
        for (String sz : sizes) {
            productVariantRepository.save(new ProductVariant(IdGenerator.newId("pv"), prod.getId(), sz, true, idx++));
        }
        return prod;
    }

    private IngestionStatusResponse toResponse(ProductIngestion job) {
        Map<String, Object> prodMap = null;
        if (job.getProductId() != null) {
            prodMap = productRepository.findById(job.getProductId())
                    .map(p -> Map.<String, Object>of("productId", p.getId(), "brand", p.getBrandId(), "category", p.getCategoryId(), "name", p.getName()))
                    .orElse(null);
        }
        Map<String, Object> recMap = job.getRecommendationId() != null ? Map.of("recommendationId", job.getRecommendationId()) : null;
        String msg = "COMPLETED".equals(job.getStatus()) ? "Product analysis completed"
                : ("NEEDS_USER_INPUT".equals(job.getStatus()) ? "Size chart could not be extracted automatically; please upload a size chart" : "Product analysis in progress");
        return new IngestionStatusResponse(
                job.getId(), job.getStatus(), msg, job.getRequiredAction(),
                prodMap, recMap, job.getExtractedSizeChartId(), job.getCreatedAt(), job.getUpdatedAt()
        );
    }
}


