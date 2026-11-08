package com.sizeok.admin;

import com.sizeok.catalog.CatalogDtos.BrandResponse;
import com.sizeok.catalog.CatalogDtos.CreateBrandRequest;
import com.sizeok.catalog.CatalogDtos.CreateProductRequest;
import com.sizeok.catalog.CatalogDtos.ProductResponse;
import com.sizeok.catalog.CatalogService;
import com.sizeok.common.security.AuthenticatedUser;
import com.sizeok.common.security.CurrentUserProvider;
import com.sizeok.feedback.FeedbackDtos.FeedbackResponse;
import com.sizeok.feedback.FeedbackService;
import com.sizeok.recommendation.controller.RecommendationDtos.RecommendationResponse;
import com.sizeok.recommendation.engine.RecommendationEngine;
import com.sizeok.sizechart.controller.SizeChartDtos.CreateSizeChartRequest;
import com.sizeok.sizechart.controller.SizeChartDtos.SizeChartResponse;
import com.sizeok.sizechart.service.SizeChartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CatalogService catalogService;
    private final SizeChartService sizeChartService;
    private final RecommendationEngine recommendationEngine;
    private final FeedbackService feedbackService;
    private final CurrentUserProvider currentUserProvider;

    public AdminController(
            CatalogService catalogService,
            SizeChartService sizeChartService,
            RecommendationEngine recommendationEngine,
            FeedbackService feedbackService,
            CurrentUserProvider currentUserProvider
    ) {
        this.catalogService = catalogService;
        this.sizeChartService = sizeChartService;
        this.recommendationEngine = recommendationEngine;
        this.feedbackService = feedbackService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse createBrand(@Valid @RequestBody CreateBrandRequest request) {
        AuthenticatedUser admin = currentUserProvider.requireAdminUser();
        return catalogService.createBrand(request, admin.userId());
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        AuthenticatedUser admin = currentUserProvider.requireAdminUser();
        return catalogService.createProduct(request, admin.userId());
    }

    @PostMapping("/size-charts")
    @ResponseStatus(HttpStatus.CREATED)
    public SizeChartResponse createSizeChart(@Valid @RequestBody CreateSizeChartRequest request) {
        AuthenticatedUser admin = currentUserProvider.requireAdminUser();
        String initialStatus = (request.markVerifiedAndActive() != null && request.markVerifiedAndActive())
                ? "ACTIVE" : "PENDING_REVIEW";
        return sizeChartService.createChart(request, admin.userId(), initialStatus);
    }

    @PatchMapping("/size-charts/{id}/status")
    public SizeChartResponse updateChartStatus(
            @PathVariable("id") String chartId,
            @RequestParam("status") String status
    ) {
        AuthenticatedUser admin = currentUserProvider.requireAdminUser();
        return sizeChartService.updateChartStatus(chartId, status, admin.userId());
    }

    @GetMapping("/recommendations/{id}/audit")
    public Map<String, Object> auditRecommendation(@PathVariable("id") String recommendationId) {
        currentUserProvider.requireAdminUser();
        RecommendationResponse rec = recommendationEngine.getRecommendationForAudit(recommendationId);
        SizeChartResponse chart = sizeChartService.getChartById(rec.sizeChartId());
        List<FeedbackResponse> feedback = feedbackService.getFeedbackForRecommendation(recommendationId);

        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("recommendation", rec);
        audit.put("chartSnapshot", chart);
        audit.put("feedbackHistory", feedback);
        return audit;
    }
}
