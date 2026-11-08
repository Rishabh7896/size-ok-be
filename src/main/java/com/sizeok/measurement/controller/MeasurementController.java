package com.sizeok.measurement.controller;

import com.sizeok.measurement.controller.MeasurementDtos.AddReferenceSizeRequest;
import com.sizeok.measurement.controller.MeasurementDtos.CreateMeasurementSetRequest;
import com.sizeok.measurement.controller.MeasurementDtos.MeasurementSetResponse;
import com.sizeok.measurement.controller.MeasurementDtos.PatchMeasurementSetRequest;
import com.sizeok.measurement.controller.MeasurementDtos.ReferenceSizeResponse;
import com.sizeok.measurement.service.MeasurementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping("/profiles/{id}/measurement-sets")
    @ResponseStatus(HttpStatus.CREATED)
    public MeasurementSetResponse saveMeasurements(
            @PathVariable("id") String profileId,
            @Valid @RequestBody CreateMeasurementSetRequest request
    ) {
        return measurementService.saveMeasurementSet(profileId, request);
    }

    @GetMapping("/profiles/{id}/measurement-sets")
    public List<MeasurementSetResponse> getMeasurementHistory(@PathVariable("id") String profileId) {
        return measurementService.getMeasurementHistory(profileId);
    }

    @GetMapping("/measurement-sets/{id}")
    public MeasurementSetResponse getMeasurementSet(@PathVariable("id") String id) {
        return measurementService.getMeasurementSet(id);
    }

    @PatchMapping("/measurement-sets/{id}")
    public MeasurementSetResponse patchMeasurementSet(
            @PathVariable("id") String id,
            @Valid @RequestBody PatchMeasurementSetRequest request
    ) {
        return measurementService.patchMeasurementSet(id, request);
    }

    @PostMapping("/profiles/{id}/reference-sizes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferenceSizeResponse addReferenceSize(
            @PathVariable("id") String profileId,
            @Valid @RequestBody AddReferenceSizeRequest request
    ) {
        return measurementService.addReferenceSize(profileId, request);
    }

    @GetMapping("/profiles/{id}/reference-sizes")
    public List<ReferenceSizeResponse> getReferenceSizes(@PathVariable("id") String profileId) {
        return measurementService.getReferenceSizes(profileId);
    }

    @DeleteMapping("/reference-sizes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReferenceSize(@PathVariable("id") String id) {
        measurementService.deleteReferenceSize(id);
    }
}
