-- =============================================================================
-- V6: Seed Levi's Size Chart and Nike / Levi's Catalog Products
-- =============================================================================

INSERT INTO size_charts (id, brand_id, product_id, category_id, region, chart_name, measurement_basis, version, status, source_type, source_url, extraction_method, confidence, verified_at, verified_by, created_at) VALUES
('chart_levis_mj_v1', 'brand_levis', NULL, 'mens_jeans', 'IN', 'Levi''s Men''s Waist & Jeans Size Guide', 'BODY', 1, 'ACTIVE', 'OFFICIAL_BRAND_GUIDE', 'https://www.levi.in/size-chart', 'ADMIN_CURATED', 0.98, CURRENT_TIMESTAMP, 'system_seed', CURRENT_TIMESTAMP);

INSERT INTO size_options (id, size_chart_id, size_label, sort_order) VALUES
('so_levis_mj_30', 'chart_levis_mj_v1', '30', 1),
('so_levis_mj_32', 'chart_levis_mj_v1', '32', 2),
('so_levis_mj_34', 'chart_levis_mj_v1', '34', 3),
('so_levis_mj_36', 'chart_levis_mj_v1', '36', 4);

INSERT INTO size_chart_measurements (id, size_option_id, dimension_code, min_value_cm, max_value_cm, original_unit, measurement_basis) VALUES
('scm_levis_30_waist', 'so_levis_mj_30', 'WAIST', 76.00, 79.00, 'CM', 'BODY'),
('scm_levis_30_hip', 'so_levis_mj_30', 'HIP', 93.00, 96.00, 'CM', 'BODY'),
('scm_levis_30_inseam', 'so_levis_mj_30', 'INSEAM', 79.00, 81.00, 'CM', 'BODY'),
('scm_levis_32_waist', 'so_levis_mj_32', 'WAIST', 81.00, 86.50, 'CM', 'BODY'),
('scm_levis_32_hip', 'so_levis_mj_32', 'HIP', 98.00, 102.00, 'CM', 'BODY'),
('scm_levis_32_inseam', 'so_levis_mj_32', 'INSEAM', 80.00, 82.00, 'CM', 'BODY'),
('scm_levis_34_waist', 'so_levis_mj_34', 'WAIST', 86.50, 91.50, 'CM', 'BODY'),
('scm_levis_34_hip', 'so_levis_mj_34', 'HIP', 103.00, 107.00, 'CM', 'BODY'),
('scm_levis_34_inseam', 'so_levis_mj_34', 'INSEAM', 81.00, 83.00, 'CM', 'BODY'),
('scm_levis_36_waist', 'so_levis_mj_36', 'WAIST', 91.50, 96.50, 'CM', 'BODY'),
('scm_levis_36_hip', 'so_levis_mj_36', 'HIP', 108.00, 112.00, 'CM', 'BODY'),
('scm_levis_36_inseam', 'so_levis_mj_36', 'INSEAM', 81.50, 83.50, 'CM', 'BODY');

INSERT INTO products (id, brand_id, category_id, retailer, name, canonical_url, product_type, fit_type, stretch_level, material_composition, gender_audience, region, created_at, updated_at) VALUES
('prod_5004', 'brand_nike', 'mens_tshirts', 'Nike', 'Nike Sportswear Club Fleece Top', 'https://www.nike.com/in/t/sportswear-club-fleece-p05004', 'T-Shirt & Top', 'REGULAR', 'MEDIUM', '80% Cotton, 20% Polyester', 'MENS', 'IN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod_5005', 'brand_levis', 'mens_jeans', 'Levi''s', 'Levi''s 501 Original Straight Fit Jeans', 'https://www.levi.in/501-original-jeans-p05005.html', 'Jeans', 'REGULAR', 'LOW', '100% Cotton Denim', 'MENS', 'IN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_variants (id, product_id, size_label, availability, sort_order) VALUES
('pv_5004_s', 'prod_5004', 'S', TRUE, 1),
('pv_5004_m', 'prod_5004', 'M', TRUE, 2),
('pv_5004_l', 'prod_5004', 'L', TRUE, 3),
('pv_5004_xl', 'prod_5004', 'XL', TRUE, 4),
('pv_5005_30', 'prod_5005', '30', TRUE, 1),
('pv_5005_32', 'prod_5005', '32', TRUE, 2),
('pv_5005_34', 'prod_5005', '34', TRUE, 3),
('pv_5005_36', 'prod_5005', '36', TRUE, 4);
