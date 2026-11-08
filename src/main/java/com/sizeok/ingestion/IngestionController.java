package com.sizeok.ingestion;

import com.sizeok.ingestion.IngestionDtos.IngestionStatusResponse;
import com.sizeok.ingestion.IngestionDtos.SubmitIngestionRequest;
import com.sizeok.ingestion.IngestionDtos.SubmitManualSizeChartRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-ingestions")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionStatusResponse submitProductUrl(@Valid @RequestBody SubmitIngestionRequest request) {
        return ingestionService.submitUrl(request);
    }

    @GetMapping("/{id}")
    public IngestionStatusResponse getIngestionStatus(@PathVariable("id") String id) {
        return ingestionService.getIngestionStatus(id);
    }

    @PostMapping("/{id}/size-chart")
    public IngestionStatusResponse submitManualChart(
            @PathVariable("id") String id,
            @Valid @RequestBody SubmitManualSizeChartRequest request
    ) {
        return ingestionService.submitManualChart(id, request);
    }

    @PostMapping("/{id}/confirm-chart")
    public IngestionStatusResponse confirmExtractedChart(@PathVariable("id") String id) {
        return ingestionService.confirmExtractedChart(id);
    }
}
