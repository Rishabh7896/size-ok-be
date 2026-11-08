-- =============================================================================
-- V4: Seed Verified Body Size Charts (Zara & Nike)
-- =============================================================================

INSERT INTO size_charts (id, brand_id, product_id, category_id, region, chart_name, measurement_basis, version, status, source_type, source_url, extraction_method, confidence, verified_at, verified_by, created_at) VALUES
('chart_zara_mt_v1', 'brand_zara', NULL, 'mens_tshirts', 'EU', 'Zara Men''s Tops Body Size Guide', 'BODY', 1, 'ACTIVE', 'OFFICIAL_BRAND_GUIDE', 'https://www.zara.com/in/en/size-guide', 'ADMIN_CURATED', 0.98, CURRENT_TIMESTAMP, 'system_seed', CURRENT_TIMESTAMP),
('chart_zara_mj_v1', 'brand_zara', NULL, 'mens_jeans', 'EU', 'Zara Men''s Jeans Size Guide', 'BODY', 1, 'ACTIVE', 'OFFICIAL_BRAND_GUIDE', 'https://www.zara.com/in/en/size-guide', 'ADMIN_CURATED', 0.98, CURRENT_TIMESTAMP, 'system_seed', CURRENT_TIMESTAMP),
('chart_nike_mt_v1', 'brand_nike', NULL, 'mens_tshirts', 'IN', 'Nike Men''s Tops Alpha Size Chart', 'BODY', 1, 'ACTIVE', 'OFFICIAL_BRAND_GUIDE', 'https://www.nike.com/in/size-fit/mens-tops-alpha', 'ADMIN_CURATED', 0.97, CURRENT_TIMESTAMP, 'system_seed', CURRENT_TIMESTAMP);

INSERT INTO size_options (id, size_chart_id, size_label, sort_order) VALUES
('so_zara_mt_s', 'chart_zara_mt_v1', 'S', 1),
('so_zara_mt_m', 'chart_zara_mt_v1', 'M', 2),
('so_zara_mt_l', 'chart_zara_mt_v1', 'L', 3),
('so_zara_mt_xl', 'chart_zara_mt_v1', 'XL', 4),
('so_zara_mj_30', 'chart_zara_mj_v1', '30', 1),
('so_zara_mj_32', 'chart_zara_mj_v1', '32', 2),
('so_zara_mj_34', 'chart_zara_mj_v1', '34', 3),
('so_zara_mj_36', 'chart_zara_mj_v1', '36', 4),
('so_nike_mt_s', 'chart_nike_mt_v1', 'S', 1),
('so_nike_mt_m', 'chart_nike_mt_v1', 'M', 2),
('so_nike_mt_l', 'chart_nike_mt_v1', 'L', 3),
('so_nike_mt_xl', 'chart_nike_mt_v1', 'XL', 4);

INSERT INTO size_chart_measurements (id, size_option_id, dimension_code, min_value_cm, max_value_cm, original_unit, measurement_basis) VALUES
('scm_zara_s_chest', 'so_zara_mt_s', 'CHEST', 91.00, 96.00, 'CM', 'BODY'),
('scm_zara_s_waist', 'so_zara_mt_s', 'WAIST', 79.00, 84.00, 'CM', 'BODY'),
('scm_zara_s_shoulder', 'so_zara_mt_s', 'SHOULDER', 42.50, 44.00, 'CM', 'BODY'),
('scm_zara_m_chest', 'so_zara_mt_m', 'CHEST', 96.00, 101.00, 'CM', 'BODY'),
('scm_zara_m_waist', 'so_zara_mt_m', 'WAIST', 84.00, 89.00, 'CM', 'BODY'),
('scm_zara_m_shoulder', 'so_zara_mt_m', 'SHOULDER', 44.00, 45.50, 'CM', 'BODY'),
('scm_zara_l_chest', 'so_zara_mt_l', 'CHEST', 101.00, 106.00, 'CM', 'BODY'),
('scm_zara_l_waist', 'so_zara_mt_l', 'WAIST', 89.00, 94.00, 'CM', 'BODY'),
('scm_zara_l_shoulder', 'so_zara_mt_l', 'SHOULDER', 45.50, 47.50, 'CM', 'BODY'),
('scm_zara_xl_chest', 'so_zara_mt_xl', 'CHEST', 106.00, 111.00, 'CM', 'BODY'),
('scm_zara_xl_waist', 'so_zara_mt_xl', 'WAIST', 94.00, 99.00, 'CM', 'BODY'),
('scm_zara_xl_shoulder', 'so_zara_mt_xl', 'SHOULDER', 47.50, 49.50, 'CM', 'BODY'),
('scm_zara_30_waist', 'so_zara_mj_30', 'WAIST', 76.00, 79.00, 'CM', 'BODY'),
('scm_zara_30_hip', 'so_zara_mj_30', 'HIP', 92.00, 96.00, 'CM', 'BODY'),
('scm_zara_30_inseam', 'so_zara_mj_30', 'INSEAM', 79.00, 81.00, 'CM', 'BODY'),
('scm_zara_32_waist', 'so_zara_mj_32', 'WAIST', 81.00, 84.00, 'CM', 'BODY'),
('scm_zara_32_hip', 'so_zara_mj_32', 'HIP', 97.00, 101.00, 'CM', 'BODY'),
('scm_zara_32_inseam', 'so_zara_mj_32', 'INSEAM', 80.00, 82.00, 'CM', 'BODY'),
('scm_zara_34_waist', 'so_zara_mj_34', 'WAIST', 86.00, 89.00, 'CM', 'BODY'),
('scm_zara_34_hip', 'so_zara_mj_34', 'HIP', 102.00, 106.00, 'CM', 'BODY'),
('scm_zara_34_inseam', 'so_zara_mj_34', 'INSEAM', 81.00, 83.00, 'CM', 'BODY'),
('scm_zara_36_waist', 'so_zara_mj_36', 'WAIST', 91.00, 94.00, 'CM', 'BODY'),
('scm_zara_36_hip', 'so_zara_mj_36', 'HIP', 107.00, 111.00, 'CM', 'BODY'),
('scm_zara_36_inseam', 'so_zara_mj_36', 'INSEAM', 81.50, 83.50, 'CM', 'BODY'),
('scm_nike_s_chest', 'so_nike_mt_s', 'CHEST', 88.00, 96.00, 'CM', 'BODY'),
('scm_nike_s_waist', 'so_nike_mt_s', 'WAIST', 73.00, 81.00, 'CM', 'BODY'),
('scm_nike_m_chest', 'so_nike_mt_m', 'CHEST', 96.00, 104.00, 'CM', 'BODY'),
('scm_nike_m_waist', 'so_nike_mt_m', 'WAIST', 81.00, 89.00, 'CM', 'BODY'),
('scm_nike_l_chest', 'so_nike_mt_l', 'CHEST', 104.00, 112.00, 'CM', 'BODY'),
('scm_nike_l_waist', 'so_nike_mt_l', 'WAIST', 89.00, 97.00, 'CM', 'BODY'),
('scm_nike_xl_chest', 'so_nike_mt_xl', 'CHEST', 112.00, 124.00, 'CM', 'BODY'),
('scm_nike_xl_waist', 'so_nike_mt_xl', 'WAIST', 97.00, 109.00, 'CM', 'BODY');
