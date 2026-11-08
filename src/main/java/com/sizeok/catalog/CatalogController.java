package com.sizeok.catalog;

import com.sizeok.catalog.CatalogDtos.BrandResponse;
import com.sizeok.catalog.CatalogDtos.CategoryNodeResponse;
import com.sizeok.catalog.CatalogDtos.ProductResponse;
import com.sizeok.sizechart.controller.SizeChartDtos.SizeChartResponse;
import com.sizeok.sizechart.service.SizeChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogService catalogService;
    private final SizeChartService sizeChartService;

    public CatalogController(CatalogService catalogService, SizeChartService sizeChartService) {
        this.catalogService = catalogService;
        this.sizeChartService = sizeChartService;
    }

    @GetMapping("/brands")
    public List<BrandResponse> searchBrands(@RequestParam(value = "q", required = false) String query) {
        return catalogService.searchBrands(query);
    }

    @GetMapping("/brands/{id}")
    public BrandResponse getBrand(@PathVariable("id") String id) {
        return catalogService.getBrand(id);
    }

    @GetMapping("/brands/{id}/size-charts")
    public List<SizeChartResponse> getBrandSizeCharts(@PathVariable("id") String id) {
        return sizeChartService.getBrandCharts(id);
    }

    @GetMapping("/categories")
    public List<CategoryNodeResponse> getCategories() {
        return catalogService.getCategoryHierarchy();
    }

    @GetMapping("/products")
    public List<ProductResponse> listProducts(
            @RequestParam(value = "brandId", required = false) String brandId,
            @RequestParam(value = "categoryId", required = false) String categoryId
    ) {
        return catalogService.searchProducts(brandId, categoryId);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable("id") String id) {
        return catalogService.getProduct(id);
    }

    @GetMapping("/products/{id}/size-charts")
    public List<SizeChartResponse> getProductSizeCharts(@PathVariable("id") String id) {
        ProductResponse prod = catalogService.getProduct(id);
        return sizeChartService.getProductCharts(id, prod.brandId(), prod.categoryId(), prod.region());
    }
}
