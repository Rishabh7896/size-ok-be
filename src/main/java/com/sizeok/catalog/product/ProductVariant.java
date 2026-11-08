package com.sizeok.catalog.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "size_label", nullable = false, length = 32)
    private String sizeLabel;

    @Column(nullable = false)
    private boolean availability;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ProductVariant() {
    }

    public ProductVariant(String id, String productId, String sizeLabel, boolean availability, int sortOrder) {
        this.id = id;
        this.productId = productId;
        this.sizeLabel = sizeLabel;
        this.availability = availability;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public boolean isAvailability() {
        return availability;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
