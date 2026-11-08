-- =============================================================================
-- V3: Seed Hierarchical Categories, Dimension Weights, Ease Models, and Brands
-- =============================================================================

INSERT INTO categories (id, parent_id, name, slug, garment_group, gender_audience, active, created_at) VALUES
('cat_apparel', NULL, 'Apparel', 'apparel', 'ROOT', 'UNISEX', TRUE, CURRENT_TIMESTAMP),
('cat_tops', 'cat_apparel', 'Tops', 'tops', 'TOPS', 'UNISEX', TRUE, CURRENT_TIMESTAMP),
('cat_bottoms', 'cat_apparel', 'Bottoms', 'bottoms', 'BOTTOMS', 'UNISEX', TRUE, CURRENT_TIMESTAMP),
('cat_one_piece', 'cat_apparel', 'One Piece', 'one-piece', 'ONE_PIECE', 'UNISEX', TRUE, CURRENT_TIMESTAMP),
('cat_innerwear', 'cat_apparel', 'Innerwear', 'innerwear', 'INNERWEAR', 'UNISEX', TRUE, CURRENT_TIMESTAMP),
('mens_tshirts', 'cat_tops', 'Men''s T-Shirts', 'mens-tshirts', 'TOPS', 'MENS', TRUE, CURRENT_TIMESTAMP),
('mens_shirts', 'cat_tops', 'Men''s Shirts', 'mens-shirts', 'TOPS', 'MENS', TRUE, CURRENT_TIMESTAMP),
('mens_jeans', 'cat_bottoms', 'Men''s Jeans', 'mens-jeans', 'BOTTOMS', 'MENS', TRUE, CURRENT_TIMESTAMP),
('mens_trousers', 'cat_bottoms', 'Men''s Trousers', 'mens-trousers', 'BOTTOMS', 'MENS', TRUE, CURRENT_TIMESTAMP),
('womens_tshirts', 'cat_tops', 'Women''s T-Shirts', 'womens-tshirts', 'TOPS', 'WOMENS', TRUE, CURRENT_TIMESTAMP),
('womens_dresses', 'cat_one_piece', 'Women''s Dresses', 'womens-dresses', 'ONE_PIECE', 'WOMENS', TRUE, CURRENT_TIMESTAMP),
('womens_jeans', 'cat_bottoms', 'Women''s Jeans', 'womens-jeans', 'BOTTOMS', 'WOMENS', TRUE, CURRENT_TIMESTAMP);

-- Men's T-Shirts: Chest 40%, Shoulder 25%, Length 15%, Waist 10%, Sleeve 10%
INSERT INTO category_dimension_weights (id, category_id, dimension_code, weight, default_ease_slim_cm, default_ease_regular_cm, default_ease_relaxed_cm, default_ease_oversized_cm, is_primary_dimension) VALUES
('cdw_mt_chest', 'mens_tshirts', 'CHEST', 0.4000, 3.00, 6.00, 10.00, 15.00, TRUE),
('cdw_mt_shoulder', 'mens_tshirts', 'SHOULDER', 0.2500, 0.50, 1.50, 3.00, 5.00, TRUE),
('cdw_mt_length', 'mens_tshirts', 'LENGTH', 0.1500, 0.00, 1.00, 2.50, 4.00, FALSE),
('cdw_mt_waist', 'mens_tshirts', 'WAIST', 0.1000, 4.00, 8.00, 12.00, 16.00, FALSE),
('cdw_mt_sleeve', 'mens_tshirts', 'SLEEVE', 0.1000, 0.50, 1.00, 2.00, 3.50, FALSE);

-- Men's Shirts: Chest 35%, Shoulder 25%, Neck 20%, Waist 10%, Sleeve 10%
INSERT INTO category_dimension_weights (id, category_id, dimension_code, weight, default_ease_slim_cm, default_ease_regular_cm, default_ease_relaxed_cm, default_ease_oversized_cm, is_primary_dimension) VALUES
('cdw_ms_chest', 'mens_shirts', 'CHEST', 0.3500, 4.00, 8.00, 12.00, 16.00, TRUE),
('cdw_ms_shoulder', 'mens_shirts', 'SHOULDER', 0.2500, 0.50, 1.50, 2.50, 4.00, TRUE),
('cdw_ms_neck', 'mens_shirts', 'NECK', 0.2000, 1.00, 1.50, 2.00, 2.50, FALSE),
('cdw_ms_waist', 'mens_shirts', 'WAIST', 0.1000, 4.00, 8.00, 12.00, 16.00, FALSE),
('cdw_ms_sleeve', 'mens_shirts', 'SLEEVE', 0.1000, 0.50, 1.00, 1.50, 2.50, FALSE);

-- Men's Jeans: Waist 50%, Hip 25%, Inseam 15%, Thigh 10%
INSERT INTO category_dimension_weights (id, category_id, dimension_code, weight, default_ease_slim_cm, default_ease_regular_cm, default_ease_relaxed_cm, default_ease_oversized_cm, is_primary_dimension) VALUES
('cdw_mj_waist', 'mens_jeans', 'WAIST', 0.5000, 1.00, 2.50, 4.00, 6.00, TRUE),
('cdw_mj_hip', 'mens_jeans', 'HIP', 0.2500, 2.00, 5.00, 8.00, 11.00, TRUE),
('cdw_mj_inseam', 'mens_jeans', 'INSEAM', 0.1500, 0.00, 0.50, 1.50, 2.50, FALSE),
('cdw_mj_thigh', 'mens_jeans', 'THIGH', 0.1000, 1.50, 3.50, 6.00, 8.50, FALSE);

-- Women's T-Shirts: Chest/Bust 45%, Waist 25%, Shoulder 20%, Hip 10%
INSERT INTO category_dimension_weights (id, category_id, dimension_code, weight, default_ease_slim_cm, default_ease_regular_cm, default_ease_relaxed_cm, default_ease_oversized_cm, is_primary_dimension) VALUES
('cdw_wt_chest', 'womens_tshirts', 'CHEST', 0.4500, 2.00, 5.00, 9.00, 14.00, TRUE),
('cdw_wt_waist', 'womens_tshirts', 'WAIST', 0.2500, 2.00, 6.00, 10.00, 14.00, FALSE),
('cdw_wt_shoulder', 'womens_tshirts', 'SHOULDER', 0.2000, 0.50, 1.00, 2.00, 3.50, TRUE),
('cdw_wt_hip', 'womens_tshirts', 'HIP', 0.1000, 2.00, 5.00, 8.00, 12.00, FALSE);

-- Verified Master Brands
INSERT INTO brands (id, name, slug, country, applicable_size_systems, official_chart_url, verification_status, active, created_at, updated_at) VALUES
('brand_zara', 'Zara', 'zara', 'ES', 'IN,EU,US,UK', 'https://www.zara.com/in/en/size-guide', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('brand_hm', 'H&M', 'hm', 'SE', 'IN,EU,US,UK', 'https://www2.hm.com/en_in/customer-service/sizeguide.html', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('brand_nike', 'Nike', 'nike', 'US', 'IN,EU,US,UK', 'https://www.nike.com/in/size-fit/mens-tops-alpha', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('brand_levis', 'Levi''s', 'levis', 'US', 'IN,EU,US,UK', 'https://www.levi.in/size-chart', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO brand_aliases (id, brand_id, alias) VALUES
('ba_zara_1', 'brand_zara', 'zara man'),
('ba_zara_2', 'brand_zara', 'zara woman'),
('ba_hm_1', 'brand_hm', 'h and m'),
('ba_hm_2', 'brand_hm', 'hennes & mauritz'),
('ba_nike_1', 'brand_nike', 'nike running'),
('ba_levis_1', 'brand_levis', 'levis');
