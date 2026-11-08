-- =============================================================================
-- V5: Seed Verified Garment Size Charts and Sample Catalog Products
-- =============================================================================

INSERT INTO size_charts (id, brand_id, product_id, category_id, region, chart_name, measurement_basis, version, status, source_type, source_url, extraction_method, confidence, verified_at, verified_by, created_at) VALUES
('chart_hm_ms_v1', 'brand_hm', NULL, 'mens_shirts', 'IN', 'H&M Men''s Garment Measurements - Shirts', 'GARMENT', 1, 'ACTIVE', 'OFFICIAL_BRAND_GUIDE', 'https://www2.hm.com/en_in/customer-service/sizeguide.html', 'ADMIN_CURATED', 0.96, CURRENT_TIMESTAMP, 'system_seed', CURRENT_TIMESTAMP);

INSERT INTO size_options (id, size_chart_id, size_label, sort_order) VALUES
('so_hm_ms_s', 'chart_hm_ms_v1', 'S', 1),
('so_hm_ms_m', 'chart_hm_ms_v1', 'M', 2),
('so_hm_ms_l', 'chart_hm_ms_v1', 'L', 3),
('so_hm_ms_xl', 'chart_hm_ms_v1', 'XL', 4);

INSERT INTO size_chart_measurements (id, size_option_id, dimension_code, min_value_cm, max_value_cm, original_unit, measurement_basis) VALUES
('scm_hm_s_chest', 'so_hm_ms_s', 'CHEST', 100.00, 100.00, 'CM', 'GARMENT'),
('scm_hm_s_shoulder', 'so_hm_ms_s', 'SHOULDER', 44.50, 44.50, 'CM', 'GARMENT'),
('scm_hm_s_waist', 'so_hm_ms_s', 'WAIST', 92.00, 92.00, 'CM', 'GARMENT'),
('scm_hm_m_chest', 'so_hm_ms_m', 'CHEST', 106.00, 106.00, 'CM', 'GARMENT'),
('scm_hm_m_shoulder', 'so_hm_ms_m', 'SHOULDER', 46.50, 46.50, 'CM', 'GARMENT'),
('scm_hm_m_waist', 'so_hm_ms_m', 'WAIST', 98.00, 98.00, 'CM', 'GARMENT'),
('scm_hm_l_chest', 'so_hm_ms_l', 'CHEST', 112.00, 112.00, 'CM', 'GARMENT'),
('scm_hm_l_shoulder', 'so_hm_ms_l', 'SHOULDER', 48.50, 48.50, 'CM', 'GARMENT'),
('scm_hm_l_waist', 'so_hm_ms_l', 'WAIST', 104.00, 104.00, 'CM', 'GARMENT'),
('scm_hm_xl_chest', 'so_hm_ms_xl', 'CHEST', 118.00, 118.00, 'CM', 'GARMENT'),
('scm_hm_xl_shoulder', 'so_hm_ms_xl', 'SHOULDER', 50.50, 50.50, 'CM', 'GARMENT'),
('scm_hm_xl_waist', 'so_hm_ms_xl', 'WAIST', 110.00, 110.00, 'CM', 'GARMENT');

INSERT INTO products (id, brand_id, category_id, retailer, name, canonical_url, product_type, fit_type, stretch_level, material_composition, gender_audience, region, created_at, updated_at) VALUES
('prod_5001', 'brand_zara', 'mens_tshirts', 'Zara', 'Zara Heavyweight Cotton Crew Neck T-Shirt', 'https://www.zara.com/in/en/heavyweight-tshirt-p05001.html', 'T-Shirt', 'REGULAR', 'LOW', '100% Cotton', 'MENS', 'EU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod_5002', 'brand_hm', 'mens_shirts', 'H&M', 'H&M Easy-Iron Regular Fit Oxford Shirt', 'https://www2.hm.com/en_in/productpage.05002.html', 'Shirt', 'REGULAR', 'NONE', '100% Cotton', 'MENS', 'IN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod_5003', 'brand_zara', 'mens_jeans', 'Zara', 'Zara Straight Fit Mid-Rise Denim Jeans', 'https://www.zara.com/in/en/straight-fit-jeans-p05003.html', 'Jeans', 'REGULAR', 'LOW', '99% Cotton, 1% Elastane', 'MENS', 'EU', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_variants (id, product_id, size_label, availability, sort_order) VALUES
('pv_5001_s', 'prod_5001', 'S', TRUE, 1),
('pv_5001_m', 'prod_5001', 'M', TRUE, 2),
('pv_5001_l', 'prod_5001', 'L', TRUE, 3),
('pv_5001_xl', 'prod_5001', 'XL', TRUE, 4),
('pv_5002_s', 'prod_5002', 'S', TRUE, 1),
('pv_5002_m', 'prod_5002', 'M', TRUE, 2),
('pv_5002_l', 'prod_5002', 'L', TRUE, 3),
('pv_5002_xl', 'prod_5002', 'XL', TRUE, 4),
('pv_5003_30', 'prod_5003', '30', TRUE, 1),
('pv_5003_32', 'prod_5003', '32', TRUE, 2),
('pv_5003_34', 'prod_5003', '34', TRUE, 3),
('pv_5003_36', 'prod_5003', '36', TRUE, 4);
