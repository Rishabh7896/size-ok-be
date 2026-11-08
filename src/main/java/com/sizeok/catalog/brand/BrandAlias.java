package com.sizeok.catalog.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand_aliases")
public class BrandAlias {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "brand_id", nullable = false, length = 64)
    private String brandId;

    @Column(nullable = false, unique = true, length = 150)
    private String alias;

    public BrandAlias() {
    }

    public BrandAlias(String id, String brandId, String alias) {
        this.id = id;
        this.brandId = brandId;
        this.alias = alias;
    }

    public String getId() {
        return id;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getAlias() {
        return alias;
    }
}
