package com.sizeok.catalog;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record BrandResponse(
            String id,
            String name,
            String slug,
            String country,
            List<String> applicableSizeSystems,
            String officialChartUrl,
            String verificationStatus,
            boolean active,
            List<String> aliases
    ) {
    }

    public record CreateBrandRequest(
            @NotBlank String name,
            String slug,
            @NotBlank String country,
            String applicableSizeSystems,
            String officialChartUrl,
            List<String> aliases
    ) {
    }

    public record CategoryNodeResponse(
            String id,
            String parentId,
            String name,
            String slug,
            String garmentGroup,
            String genderAudience,
            List<CategoryNodeResponse> children
    ) {
    }

    public record ProductVariantDto(
            String sizeLabel,
            boolean available,
            int sortOrder
    ) {
    }

    public record ProductResponse(
            String id,
            String brandId,
            String brandName,
            String categoryId,
            String categoryName,
            String retailer,
            String name,
            String canonicalUrl,
            String productType,
            String fitType,
            String stretchLevel,
            String materialComposition,
            String genderAudience,
            String region,
            List<ProductVariantDto> availableSizes,
            OffsetDateTime createdAt
    ) {
    }

    public record CreateProductRequest(
            @NotBlank String brandId,
            @NotBlank String categoryId,
            @NotBlank String retailer,
            @NotBlank String name,
            String canonicalUrl,
            String productType,
            String fitType,
            String stretchLevel,
            String materialComposition,
            String genderAudience,
            String region,
            List<String> sizes
    ) {
    }
}
