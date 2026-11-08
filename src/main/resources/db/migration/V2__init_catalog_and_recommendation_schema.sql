-- =============================================================================
-- V2: Catalog, Size Charts, Reference Sizes, Ingestion, Recommendations, Feedback
-- =============================================================================

CREATE TABLE brands (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    country VARCHAR(64) NOT NULL,
    applicable_size_systems VARCHAR(255) NOT NULL DEFAULT 'IN,EU,US,UK',
    official_chart_url VARCHAR(500),
    verification_status VARCHAR(32) NOT NULL DEFAULT 'VERIFIED',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE brand_aliases (
    id VARCHAR(64) PRIMARY KEY,
    brand_id VARCHAR(64) NOT NULL REFERENCES brands(id) ON DELETE CASCADE,
    alias VARCHAR(150) NOT NULL UNIQUE
);

CREATE INDEX idx_brand_aliases_brand_id ON brand_aliases(brand_id);

CREATE TABLE categories (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64) REFERENCES categories(id),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    garment_group VARCHAR(64) NOT NULL,
    gender_audience VARCHAR(32) NOT NULL DEFAULT 'UNISEX',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);

CREATE TABLE category_dimension_weights (
    id VARCHAR(64) PRIMARY KEY,
    category_id VARCHAR(64) NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    dimension_code VARCHAR(64) NOT NULL,
    weight NUMERIC(5,4) NOT NULL,
    default_ease_slim_cm NUMERIC(6,2) NOT NULL DEFAULT 2.00,
    default_ease_regular_cm NUMERIC(6,2) NOT NULL DEFAULT 6.00,
    default_ease_relaxed_cm NUMERIC(6,2) NOT NULL DEFAULT 10.00,
    default_ease_oversized_cm NUMERIC(6,2) NOT NULL DEFAULT 14.00,
    is_primary_dimension BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_category_dimension_weight UNIQUE (category_id, dimension_code)
);

CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    brand_id VARCHAR(64) NOT NULL REFERENCES brands(id),
    category_id VARCHAR(64) NOT NULL REFERENCES categories(id),
    retailer VARCHAR(120) NOT NULL,
    name VARCHAR(255) NOT NULL,
    canonical_url VARCHAR(1000) UNIQUE,
    product_type VARCHAR(100),
    fit_type VARCHAR(32) NOT NULL DEFAULT 'REGULAR',
    stretch_level VARCHAR(32) NOT NULL DEFAULT 'LOW',
    material_composition VARCHAR(255),
    gender_audience VARCHAR(32) NOT NULL DEFAULT 'UNISEX',
    region VARCHAR(16) NOT NULL DEFAULT 'IN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_products_brand_category ON products(brand_id, category_id);

CREATE TABLE product_variants (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size_label VARCHAR(32) NOT NULL,
    availability BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_product_variants_size UNIQUE (product_id, size_label)
);

CREATE TABLE size_charts (
    id VARCHAR(64) PRIMARY KEY,
    brand_id VARCHAR(64) NOT NULL REFERENCES brands(id),
    product_id VARCHAR(64) REFERENCES products(id),
    category_id VARCHAR(64) NOT NULL REFERENCES categories(id),
    region VARCHAR(16) NOT NULL DEFAULT 'IN',
    chart_name VARCHAR(255) NOT NULL,
    measurement_basis VARCHAR(32) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    source_url VARCHAR(1000),
    extraction_method VARCHAR(64) NOT NULL,
    confidence NUMERIC(4,2) NOT NULL DEFAULT 1.00,
    extracted_at TIMESTAMP WITH TIME ZONE,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_size_charts_lookup ON size_charts(brand_id, category_id, region, status);
CREATE INDEX idx_size_charts_product ON size_charts(product_id, status);

CREATE TABLE size_options (
    id VARCHAR(64) PRIMARY KEY,
    size_chart_id VARCHAR(64) NOT NULL REFERENCES size_charts(id) ON DELETE CASCADE,
    size_label VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT uq_size_options_chart_label UNIQUE (size_chart_id, size_label)
);

CREATE TABLE size_chart_measurements (
    id VARCHAR(64) PRIMARY KEY,
    size_option_id VARCHAR(64) NOT NULL REFERENCES size_options(id) ON DELETE CASCADE,
    dimension_code VARCHAR(64) NOT NULL,
    min_value_cm NUMERIC(8,2) NOT NULL,
    max_value_cm NUMERIC(8,2) NOT NULL,
    original_unit VARCHAR(16) NOT NULL DEFAULT 'CM',
    measurement_basis VARCHAR(32) NOT NULL,
    CONSTRAINT uq_size_chart_measurements_opt_dim UNIQUE (size_option_id, dimension_code)
);

CREATE TABLE reference_sizes (
    id VARCHAR(64) PRIMARY KEY,
    profile_id VARCHAR(64) NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    brand_id VARCHAR(64) NOT NULL REFERENCES brands(id),
    category_id VARCHAR(64) NOT NULL REFERENCES categories(id),
    product_id VARCHAR(64) REFERENCES products(id),
    size_chart_id VARCHAR(64) REFERENCES size_charts(id),
    size_label VARCHAR(32) NOT NULL,
    region VARCHAR(16) NOT NULL DEFAULT 'IN',
    fit_preference VARCHAR(32) NOT NULL DEFAULT 'REGULAR',
    inferred_ranges_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_reference_sizes_profile_cat ON reference_sizes(profile_id, category_id);

CREATE TABLE product_ingestions (
    id VARCHAR(64) PRIMARY KEY,
    profile_id VARCHAR(64) REFERENCES profiles(id),
    submitted_url VARCHAR(1000) NOT NULL,
    retailer VARCHAR(120),
    fit_preference VARCHAR(32) NOT NULL DEFAULT 'REGULAR',
    status VARCHAR(32) NOT NULL,
    required_action VARCHAR(64),
    parser_version VARCHAR(32) NOT NULL DEFAULT '1.0',
    failure_reason VARCHAR(500),
    product_id VARCHAR(64) REFERENCES products(id),
    extracted_size_chart_id VARCHAR(64) REFERENCES size_charts(id),
    recommendation_id VARCHAR(64),
    idempotency_key VARCHAR(128) UNIQUE,
    raw_metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE recommendations (
    id VARCHAR(64) PRIMARY KEY,
    profile_id VARCHAR(64) NOT NULL REFERENCES profiles(id),
    product_id VARCHAR(64) NOT NULL REFERENCES products(id),
    measurement_set_id VARCHAR(64) REFERENCES measurement_sets(id),
    measurement_set_version INT,
    size_chart_id VARCHAR(64) NOT NULL REFERENCES size_charts(id),
    chart_version INT NOT NULL,
    fit_preference VARCHAR(32) NOT NULL,
    recommended_size VARCHAR(32),
    alternative_size VARCHAR(32),
    fit_score INT NOT NULL,
    confidence VARCHAR(32) NOT NULL,
    fit_summary VARCHAR(255) NOT NULL,
    dimension_analysis_json TEXT NOT NULL,
    explanation_json TEXT NOT NULL,
    snapshot_measurements_json TEXT NOT NULL,
    algorithm_version VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_recommendations_profile_id ON recommendations(profile_id);

CREATE TABLE recommendation_candidates (
    id VARCHAR(64) PRIMARY KEY,
    recommendation_id VARCHAR(64) NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    size_label VARCHAR(32) NOT NULL,
    fit_score INT NOT NULL,
    rank_order INT NOT NULL,
    dimension_breakdown_json TEXT
);

CREATE TABLE fit_feedback (
    id VARCHAR(64) PRIMARY KEY,
    recommendation_id VARCHAR(64) NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    purchased BOOLEAN NOT NULL,
    actual_size VARCHAR(32) NOT NULL,
    kept_size VARCHAR(32),
    fit_result VARCHAR(32) NOT NULL,
    returned_due_to_size BOOLEAN NOT NULL DEFAULT FALSE,
    problem_dimensions VARCHAR(255),
    user_comment VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

