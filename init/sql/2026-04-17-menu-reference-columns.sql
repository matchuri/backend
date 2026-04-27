-- Matchuri manual migration
-- Purpose:
-- 1. Add stable business key and active lifecycle fields to menu_items
-- 2. Add active lifecycle and sort order fields to attribute_categories
-- 3. Add active lifecycle and sort order fields to ingredients
--
-- Note:
-- The current application still uses Hibernate ddl-auto update in local/dev.
-- This script is provided as an explicit manual migration reference for shared DBs
-- or controlled rollout environments.

ALTER TABLE menu_items
    ADD COLUMN code VARCHAR(50) NOT NULL COMMENT '메뉴 코드' AFTER id,
    ADD COLUMN is_active BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성 여부' AFTER description;

ALTER TABLE menu_items
    ADD CONSTRAINT uk_menu_items_code UNIQUE (code);

CREATE INDEX idx_menu_items_active ON menu_items (is_active);

ALTER TABLE attribute_categories
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서' AFTER name,
    ADD COLUMN is_active BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성 여부' AFTER sort_order;

CREATE INDEX idx_attribute_categories_active ON attribute_categories (is_active);
CREATE INDEX idx_attribute_categories_type_active ON attribute_categories (category_type, is_active);

ALTER TABLE ingredients
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서' AFTER is_allergen,
    ADD COLUMN is_active BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성 여부' AFTER sort_order;

CREATE INDEX idx_ingredients_active ON ingredients (is_active);
