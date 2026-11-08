package com.sizeok.catalog;

import com.sizeok.catalog.CatalogDtos.BrandResponse;
import com.sizeok.catalog.CatalogDtos.CategoryNodeResponse;
import com.sizeok.catalog.CatalogDtos.CreateBrandRequest;
import com.sizeok.catalog.CatalogDtos.CreateProductRequest;
import com.sizeok.catalog.CatalogDtos.ProductResponse;
import com.sizeok.catalog.CatalogDtos.ProductVariantDto;
import com.sizeok.catalog.brand.Brand;
import com.sizeok.catalog.brand.BrandAlias;
import com.sizeok.catalog.brand.BrandAliasRepository;
import com.sizeok.catalog.brand.BrandRepository;
import com.sizeok.catalog.category.Category;
import com.sizeok.catalog.category.CategoryRepository;
import com.sizeok.catalog.product.Product;
import com.sizeok.catalog.product.ProductRepository;
import com.sizeok.catalog.product.ProductVariant;
import com.sizeok.catalog.product.ProductVariantRepository;
import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final BrandRepository brandRepository;
    private final BrandAliasRepository brandAliasRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AuditService auditService;

    public CatalogService(
            BrandRepository brandRepository,
            BrandAliasRepository brandAliasRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            AuditService auditService
    ) {
        this.brandRepository = brandRepository;
        this.brandAliasRepository = brandAliasRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> searchBrands(String query) {
        List<Brand> brands = (query == null || query.isBlank())
                ? brandRepository.findByActiveTrueOrderByNameAsc()
                : brandRepository.findByNameContainingIgnoreCaseAndActiveTrueOrderByNameAsc(query.trim());
        return brands.stream().map(this::toBrandResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrand(String brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", brandId));
        return toBrandResponse(brand);
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest req, String actorId) {
        String slug = (req.slug() != null && !req.slug().isBlank())
                ? req.slug().trim().toLowerCase()
                : req.name().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        if (brandRepository.findBySlugIgnoreCase(slug).isPresent()) {
            throw new BadRequestException("BRAND_ALREADY_EXISTS", "Brand already exists with slug: " + slug);
        }
        OffsetDateTime now = OffsetDateTime.now();
        Brand brand = new Brand(
                "brand_" + slug.replace("-", "_"),
                req.name().trim(),
                slug,
                req.country().trim().toUpperCase(),
                req.applicableSizeSystems() != null ? req.applicableSizeSystems() : "IN,EU,US,UK",
                req.officialChartUrl(),
                "VERIFIED",
                true,
                now,
                now
        );
        brandRepository.save(brand);
        if (req.aliases() != null) {
            for (String alias : req.aliases()) {
                if (alias != null && !alias.isBlank()) {
                    brandAliasRepository.save(new BrandAlias(IdGenerator.newId("ba"), brand.getId(), alias.trim().toLowerCase()));
                }
            }
        }
        auditService.record(actorId, "BRAND_CREATED", "BRAND", brand.getId(), null);
        return toBrandResponse(brand);
    }

    @Transactional(readOnly = true)
    public List<CategoryNodeResponse> getCategoryHierarchy() {
        List<Category> all = categoryRepository.findByActiveTrueOrderByNameAsc();
        Map<String, List<Category>> byParent = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(root -> buildNode(root, byParent))
                .toList();
    }

    private CategoryNodeResponse buildNode(Category cat, Map<String, List<Category>> byParent) {
        List<CategoryNodeResponse> children = byParent.getOrDefault(cat.getId(), List.of())
                .stream()
                .map(child -> buildNode(child, byParent))
                .toList();
        return new CategoryNodeResponse(cat.getId(), cat.getParentId(), cat.getName(), cat.getSlug(), cat.getGarmentGroup(), cat.getGenderAudience(), children);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String brandId, String categoryId) {
        List<Product> products;
        if (brandId != null && !brandId.isBlank()) {
            products = productRepository.findByBrandIdOrderByCreatedAtDesc(brandId);
        } else if (categoryId != null && !categoryId.isBlank()) {
            products = productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::toProductResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return toProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest req, String actorId) {
        Brand brand = brandRepository.findById(req.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", req.brandId()));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.categoryId()));

        OffsetDateTime now = OffsetDateTime.now();
        Product product = new Product(
                IdGenerator.newId("prod"),
                brand.getId(),
                category.getId(),
                req.retailer().trim(),
                req.name().trim(),
                req.canonicalUrl(),
                req.productType() != null ? req.productType() : category.getName(),
                req.fitType() != null ? req.fitType().trim().toUpperCase() : "REGULAR",
                req.stretchLevel() != null ? req.stretchLevel().trim().toUpperCase() : "LOW",
                req.materialComposition(),
                req.genderAudience() != null ? req.genderAudience().trim().toUpperCase() : category.getGenderAudience(),
                req.region() != null ? req.region().trim().toUpperCase() : "IN",
                now,
                now
        );
        productRepository.save(product);

        List<String> sizes = (req.sizes() != null && !req.sizes().isEmpty()) ? req.sizes() : List.of("S", "M", "L", "XL");
        int order = 1;
        for (String size : sizes) {
            productVariantRepository.save(new ProductVariant(
                    IdGenerator.newId("pv"), product.getId(), size.trim().toUpperCase(), true, order++
            ));
        }
        auditService.record(actorId, "PRODUCT_CREATED", "PRODUCT", product.getId(), null);
        return toProductResponse(product);
    }

    public BrandResponse toBrandResponse(Brand brand) {
        List<String> aliases = brandAliasRepository.findByBrandId(brand.getId())
                .stream()
                .map(BrandAlias::getAlias)
                .toList();
        List<String> systems = Arrays.stream(brand.getApplicableSizeSystems().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return new BrandResponse(
                brand.getId(), brand.getName(), brand.getSlug(), brand.getCountry(),
                systems, brand.getOfficialChartUrl(), brand.getVerificationStatus(), brand.isActive(), aliases
        );
    }

    public ProductResponse toProductResponse(Product product) {
        String brandName = brandRepository.findById(product.getBrandId()).map(Brand::getName).orElse(product.getBrandId());
        String categoryName = categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse(product.getCategoryId());
        List<ProductVariantDto> variants = productVariantRepository.findByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(v -> new ProductVariantDto(v.getSizeLabel(), v.isAvailability(), v.getSortOrder()))
                .toList();
        return new ProductResponse(
                product.getId(), product.getBrandId(), brandName, product.getCategoryId(), categoryName,
                product.getRetailer(), product.getName(), product.getCanonicalUrl(), product.getProductType(),
                product.getFitType(), product.getStretchLevel(), product.getMaterialComposition(),
                product.getGenderAudience(), product.getRegion(), variants, product.getCreatedAt()
        );
    }
}

