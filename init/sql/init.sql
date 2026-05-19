-- Matchuri local MySQL bootstrap.
-- Docker executes files in /docker-entrypoint-initdb.d only when the data directory is empty.

SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50),
    password_hash VARCHAR(255),
    nickname VARCHAR(100),
    nickname_completed BIT(1) NOT NULL DEFAULT b'0',
    email VARCHAR(150),
    is_social BIT(1) NOT NULL DEFAULT b'0',
    social_provider_type VARCHAR(20),
    social_provider_user_id VARCHAR(100),
    member_role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_members_login_id UNIQUE (login_id),
    CONSTRAINT uk_members_nickname UNIQUE (nickname),
    CONSTRAINT uk_members_social_provider_user UNIQUE (social_provider_type, social_provider_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attribute_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_type VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_attribute_categories_type_code UNIQUE (category_type, code),
    INDEX idx_attribute_categories_active (is_active),
    INDEX idx_attribute_categories_type_active (category_type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ingredients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_allergen BIT(1) NOT NULL,
    sort_order INT NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_ingredients_code UNIQUE (code),
    INDEX idx_ingredients_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    is_active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_menu_items_code UNIQUE (code),
    INDEX idx_menu_items_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_attribute_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    attribute_category_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_menu_attribute_category UNIQUE (menu_id, attribute_category_id),
    INDEX idx_menu_attribute_categories_category (attribute_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_ingredients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_menu_ingredient UNIQUE (menu_id, ingredient_id),
    INDEX idx_menu_ingredients_ingredient (ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_agreements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    agreement_type VARCHAR(50) NOT NULL,
    agreement_version VARCHAR(50) NOT NULL,
    agreed_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_agreements_member_type_version UNIQUE (member_id, agreement_type, agreement_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_taste_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    profile_version VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_taste_profiles_member UNIQUE (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_taste_profile_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    attribute_category_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_taste_profile_category UNIQUE (profile_id, attribute_category_id),
    INDEX idx_member_taste_profile_categories_category (attribute_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_taste_profile_restriction_ingredients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_profile_restriction_ingredient UNIQUE (profile_id, ingredient_id),
    INDEX idx_member_taste_profile_restriction_ingredients_ingredient (ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_taste_profile_disliked_menu_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_profile_disliked_menu_item UNIQUE (profile_id, menu_id),
    INDEX idx_member_taste_profile_disliked_menu_items_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_refresh_tokens_token UNIQUE (token),
    INDEX idx_auth_refresh_tokens_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_exchange_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    code VARCHAR(128) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_exchange_codes_code UNIQUE (code),
    INDEX idx_auth_exchange_codes_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_email_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    login_id VARCHAR(50),
    purpose VARCHAR(30) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6),
    verification_token_hash VARCHAR(255),
    verification_token_expires_at DATETIME(6),
    verification_token_used_at DATETIME(6),
    attempt_count INT NOT NULL,
    last_sent_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_email_verifications_token_hash UNIQUE (verification_token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE personal_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    context_json JSON,
    selected_candidate_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_personal_recommendations_member (member_id),
    INDEX idx_personal_recommendations_selected_candidate (selected_candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE personal_recommendation_candidates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    personal_recommendation_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    rank_no INT NOT NULL,
    score DOUBLE,
    candidate_meta_json JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_personal_recommendation_candidate_menu UNIQUE (personal_recommendation_id, menu_id),
    INDEX idx_personal_recommendation_candidates_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_menu_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    personal_recommendation_id BIGINT,
    action_type VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_member_menu_actions_member (member_id),
    INDEX idx_member_menu_actions_menu (menu_id),
    INDEX idx_member_menu_actions_personal_recommendation (personal_recommendation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    invite_code VARCHAR(32) NOT NULL,
    host_member_id BIGINT NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_group_rooms_invite_code UNIQUE (invite_code),
    INDEX idx_group_rooms_host_member (host_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_room_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    left_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_group_room_member UNIQUE (room_id, member_id),
    INDEX idx_group_room_members_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_invites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    request_member_id BIGINT NOT NULL,
    target_member_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at DATETIME(6),
    responded_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_group_invites_room (room_id),
    INDEX idx_group_invites_request_member (request_member_id),
    INDEX idx_group_invites_target_member (target_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6),
    selected_candidate_id BIGINT,
    context_json JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_group_recommendations_room (room_id),
    INDEX idx_group_recommendations_selected_candidate (selected_candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_recommendation_candidates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_recommendation_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    rank_no INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_group_recommendation_candidate_menu UNIQUE (group_recommendation_id, menu_id),
    INDEX idx_group_recommendation_candidates_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_recommendation_votes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_recommendation_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_group_recommendation_vote_member UNIQUE (group_recommendation_id, member_id),
    INDEX idx_group_recommendation_votes_candidate (candidate_id),
    INDEX idx_group_recommendation_votes_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_presence_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    websocket_session_id VARCHAR(100),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_group_presence_events_room (room_id),
    INDEX idx_group_presence_events_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE menu_attribute_categories
    ADD CONSTRAINT fk_menu_attribute_categories_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id),
    ADD CONSTRAINT fk_menu_attribute_categories_category FOREIGN KEY (attribute_category_id) REFERENCES attribute_categories (id);

ALTER TABLE menu_ingredients
    ADD CONSTRAINT fk_menu_ingredients_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id),
    ADD CONSTRAINT fk_menu_ingredients_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id);

ALTER TABLE member_agreements
    ADD CONSTRAINT fk_member_agreements_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE member_taste_profiles
    ADD CONSTRAINT fk_member_taste_profiles_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE member_taste_profile_categories
    ADD CONSTRAINT fk_member_taste_profile_categories_profile FOREIGN KEY (profile_id) REFERENCES member_taste_profiles (id),
    ADD CONSTRAINT fk_member_taste_profile_categories_category FOREIGN KEY (attribute_category_id) REFERENCES attribute_categories (id);

ALTER TABLE member_taste_profile_restriction_ingredients
    ADD CONSTRAINT fk_member_taste_profile_restriction_ingredients_profile FOREIGN KEY (profile_id) REFERENCES member_taste_profiles (id),
    ADD CONSTRAINT fk_member_taste_profile_restriction_ingredients_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id);

ALTER TABLE member_taste_profile_disliked_menu_items
    ADD CONSTRAINT fk_member_taste_profile_disliked_menu_items_profile FOREIGN KEY (profile_id) REFERENCES member_taste_profiles (id),
    ADD CONSTRAINT fk_member_taste_profile_disliked_menu_items_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id);

ALTER TABLE auth_refresh_tokens
    ADD CONSTRAINT fk_auth_refresh_tokens_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE auth_exchange_codes
    ADD CONSTRAINT fk_auth_exchange_codes_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE personal_recommendations
    ADD CONSTRAINT fk_personal_recommendations_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE personal_recommendation_candidates
    ADD CONSTRAINT fk_personal_recommendation_candidates_recommendation FOREIGN KEY (personal_recommendation_id) REFERENCES personal_recommendations (id),
    ADD CONSTRAINT fk_personal_recommendation_candidates_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id);

ALTER TABLE personal_recommendations
    ADD CONSTRAINT fk_personal_recommendations_selected_candidate FOREIGN KEY (selected_candidate_id) REFERENCES personal_recommendation_candidates (id);

ALTER TABLE member_menu_actions
    ADD CONSTRAINT fk_member_menu_actions_member FOREIGN KEY (member_id) REFERENCES members (id),
    ADD CONSTRAINT fk_member_menu_actions_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id),
    ADD CONSTRAINT fk_member_menu_actions_personal_recommendation FOREIGN KEY (personal_recommendation_id) REFERENCES personal_recommendations (id);

ALTER TABLE group_rooms
    ADD CONSTRAINT fk_group_rooms_host_member FOREIGN KEY (host_member_id) REFERENCES members (id);

ALTER TABLE group_room_members
    ADD CONSTRAINT fk_group_room_members_room FOREIGN KEY (room_id) REFERENCES group_rooms (id),
    ADD CONSTRAINT fk_group_room_members_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE group_invites
    ADD CONSTRAINT fk_group_invites_room FOREIGN KEY (room_id) REFERENCES group_rooms (id),
    ADD CONSTRAINT fk_group_invites_request_member FOREIGN KEY (request_member_id) REFERENCES members (id),
    ADD CONSTRAINT fk_group_invites_target_member FOREIGN KEY (target_member_id) REFERENCES members (id);

ALTER TABLE group_recommendations
    ADD CONSTRAINT fk_group_recommendations_room FOREIGN KEY (room_id) REFERENCES group_rooms (id);

ALTER TABLE group_recommendation_candidates
    ADD CONSTRAINT fk_group_recommendation_candidates_recommendation FOREIGN KEY (group_recommendation_id) REFERENCES group_recommendations (id),
    ADD CONSTRAINT fk_group_recommendation_candidates_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id);

ALTER TABLE group_recommendations
    ADD CONSTRAINT fk_group_recommendations_selected_candidate FOREIGN KEY (selected_candidate_id) REFERENCES group_recommendation_candidates (id);

ALTER TABLE group_recommendation_votes
    ADD CONSTRAINT fk_group_recommendation_votes_recommendation FOREIGN KEY (group_recommendation_id) REFERENCES group_recommendations (id),
    ADD CONSTRAINT fk_group_recommendation_votes_candidate FOREIGN KEY (candidate_id) REFERENCES group_recommendation_candidates (id),
    ADD CONSTRAINT fk_group_recommendation_votes_member FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE group_presence_events
    ADD CONSTRAINT fk_group_presence_events_room FOREIGN KEY (room_id) REFERENCES group_rooms (id),
    ADD CONSTRAINT fk_group_presence_events_member FOREIGN KEY (member_id) REFERENCES members (id);

-- Reference seed data

INSERT INTO attribute_categories (category_type, code, name, sort_order, is_active, created_at, updated_at) VALUES
    ('FLAVOR', 'SPICY', '매콤', 10, b'1', NOW(6), NOW(6)),
    ('FLAVOR', 'SWEET', '달콤', 20, b'1', NOW(6), NOW(6)),
    ('FLAVOR', 'SALTY', '짭짤', 30, b'1', NOW(6), NOW(6)),
    ('FLAVOR', 'NUTTY', '고소', 40, b'1', NOW(6), NOW(6)),
    ('FLAVOR', 'FRESH', '상큼', 50, b'1', NOW(6), NOW(6)),
    ('FLAVOR', 'RICH', '진한/묵직한', 60, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'SOUP', '국물/탕', 10, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'GRILLED', '구이', 20, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'FRIED', '튀김', 30, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'STIR_FRIED', '볶음', 40, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'STEAMED', '찜', 50, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'RAW_SALAD', '생식/샐러드', 60, b'1', NOW(6), NOW(6)),
    ('COOKING_METHOD', 'NOODLE_MIXED', '면/비빔', 70, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'KOREAN', '한식', 10, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'CHINESE', '중식', 20, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'JAPANESE', '일식', 30, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'WESTERN', '양식', 40, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'BUNSIK', '분식', 50, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'FAST_FOOD', '패스트푸드', 60, b'1', NOW(6), NOW(6)),
    ('FOOD_CATEGORY', 'ASIAN', '아시안', 70, b'1', NOW(6), NOW(6)),
    ('TEXTURE', 'CRISPY', '바삭', 10, b'1', NOW(6), NOW(6)),
    ('TEXTURE', 'CHEWY', '쫄깃', 20, b'1', NOW(6), NOW(6)),
    ('TEXTURE', 'CRUNCHY', '아삭', 30, b'1', NOW(6), NOW(6)),
    ('TEXTURE', 'SOFT', '부드러움', 40, b'1', NOW(6), NOW(6)),
    ('TEMPERATURE', 'HOT', '뜨거움', 10, b'1', NOW(6), NOW(6)),
    ('TEMPERATURE', 'COLD', '차가움', 20, b'1', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order),
    is_active = VALUES(is_active),
    updated_at = VALUES(updated_at);

INSERT INTO ingredients (code, name, is_allergen, sort_order, is_active, created_at, updated_at) VALUES
    ('EGG', '계란', b'1', 10, b'1', NOW(6), NOW(6)),
    ('MILK', '우유', b'1', 20, b'1', NOW(6), NOW(6)),
    ('BUCKWHEAT', '메밀', b'1', 30, b'1', NOW(6), NOW(6)),
    ('PEANUT', '땅콩', b'1', 40, b'1', NOW(6), NOW(6)),
    ('SOYBEAN', '대두', b'1', 50, b'1', NOW(6), NOW(6)),
    ('WHEAT', '밀', b'1', 60, b'1', NOW(6), NOW(6)),
    ('PINE_NUT', '잣', b'1', 70, b'1', NOW(6), NOW(6)),
    ('WALNUT', '호두', b'1', 80, b'1', NOW(6), NOW(6)),
    ('CRAB', '게', b'1', 90, b'1', NOW(6), NOW(6)),
    ('SHRIMP', '새우', b'1', 100, b'1', NOW(6), NOW(6)),
    ('SQUID', '오징어', b'1', 110, b'1', NOW(6), NOW(6)),
    ('MACKEREL', '고등어', b'1', 120, b'1', NOW(6), NOW(6)),
    ('SHELLFISH', '조개류', b'1', 130, b'1', NOW(6), NOW(6)),
    ('PEACH', '복숭아', b'1', 140, b'1', NOW(6), NOW(6)),
    ('TOMATO', '토마토', b'1', 150, b'1', NOW(6), NOW(6)),
    ('CHICKEN', '닭고기', b'1', 160, b'1', NOW(6), NOW(6)),
    ('PORK', '돼지고기', b'1', 170, b'1', NOW(6), NOW(6)),
    ('BEEF', '쇠고기', b'1', 180, b'1', NOW(6), NOW(6)),
    ('SULFITES', '아황산류', b'1', 190, b'1', NOW(6), NOW(6)),
    ('KIWI', '키위', b'1', 200, b'1', NOW(6), NOW(6)),
    ('CASHEW_NUT', '캐슈넛', b'1', 210, b'1', NOW(6), NOW(6)),
    ('PUPA', '번데기', b'1', 220, b'1', NOW(6), NOW(6)),
    ('YAM', '마', b'1', 230, b'1', NOW(6), NOW(6)),
    ('SESAME', '참깨', b'1', 240, b'1', NOW(6), NOW(6)),
    ('FISH', '생선', b'0', 310, b'1', NOW(6), NOW(6)),
    ('TOFU', '두부', b'0', 320, b'1', NOW(6), NOW(6)),
    ('KIMCHI', '김치', b'0', 330, b'1', NOW(6), NOW(6)),
    ('CHEESE', '치즈', b'0', 340, b'1', NOW(6), NOW(6)),
    ('CILANTRO', '고수', b'0', 350, b'1', NOW(6), NOW(6)),
    ('EGGPLANT', '가지', b'0', 360, b'1', NOW(6), NOW(6)),
    ('MUSHROOM', '버섯류', b'0', 370, b'1', NOW(6), NOW(6)),
    ('CUCUMBER', '오이', b'0', 380, b'1', NOW(6), NOW(6)),
    ('GREEN_PEPPER', '피망', b'0', 390, b'1', NOW(6), NOW(6)),
    ('PAPRIKA', '파프리카', b'0', 400, b'1', NOW(6), NOW(6)),
    ('BROCCOLI', '브로콜리', b'0', 410, b'1', NOW(6), NOW(6)),
    ('ONION', '양파', b'0', 420, b'1', NOW(6), NOW(6)),
    ('GARLIC', '마늘', b'0', 430, b'1', NOW(6), NOW(6)),
    ('GREEN_ONION', '파', b'0', 440, b'1', NOW(6), NOW(6)),
    ('GARLIC_CHIVE', '부추', b'0', 450, b'1', NOW(6), NOW(6)),
    ('WATER_PARSLEY', '미나리', b'0', 460, b'1', NOW(6), NOW(6)),
    ('PERILLA_LEAF', '들깻잎', b'0', 470, b'1', NOW(6), NOW(6)),
    ('CELERY', '셀러리', b'0', 480, b'1', NOW(6), NOW(6)),
    ('OLIVE', '올리브', b'0', 490, b'1', NOW(6), NOW(6)),
    ('BLUE_CHEESE', '블루치즈류', b'0', 500, b'1', NOW(6), NOW(6)),
    ('OFFAL', '내장류', b'0', 510, b'1', NOW(6), NOW(6)),
    ('STRONG_FISHY_FISH', '생선 비린 향 강한 재료', b'0', 520, b'1', NOW(6), NOW(6)),
    ('STRONG_SEAFOOD_AROMA', '해산물 향 강한 재료', b'0', 530, b'1', NOW(6), NOW(6)),
    ('AROMATIC_SPICES', '향신채/향신료 계열', b'0', 540, b'1', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_allergen = VALUES(is_allergen),
    sort_order = VALUES(sort_order),
    is_active = VALUES(is_active),
    updated_at = VALUES(updated_at);

INSERT INTO menu_items (code, name, description, is_active, created_at, updated_at) VALUES
    ('KIMCHI_STEW', '김치찌개', '김치와 돼지고기를 넣고 끓인 대표적인 한식 찌개입니다.', b'1', NOW(6), NOW(6)),
    ('DOENJANG_STEW', '된장찌개', '된장을 기본으로 두부와 채소를 넣어 끓인 구수한 찌개입니다.', b'1', NOW(6), NOW(6)),
    ('BIBIMBAP', '비빔밥', '밥 위에 여러 나물과 고추장을 올려 비벼 먹는 한식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('PORK_CUTLET', '돈까스', '돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('JJAJANGMYEON', '짜장면', '춘장 소스에 면을 비벼 먹는 중식 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('JJAMPPONG', '짬뽕', '해산물과 채소를 넣은 매콤한 국물 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('RAMEN', '라멘', '진한 육수와 면을 함께 즐기는 일본식 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('SUSHI', '초밥', '초밥용 밥 위에 생선이나 재료를 올린 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('BULGOGI', '불고기', '얇게 썬 소고기를 달콤짭짤한 양념에 볶거나 구운 한식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('SAMGYEOPSAL', '삼겹살', '돼지고기를 구워 쌈과 곁들여 먹는 구이 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('DAK_GALBI', '닭갈비', '닭고기와 채소를 매콤한 양념에 볶아 먹는 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('BUDAE_JJIGAE', '부대찌개', '햄, 소시지, 김치, 라면 사리를 넣어 끓이는 진한 찌개입니다.', b'1', NOW(6), NOW(6)),
    ('GALBITANG', '갈비탕', '소갈비를 오래 끓여 맑고 깊은 국물로 즐기는 한식 탕입니다.', b'1', NOW(6), NOW(6)),
    ('NAENGMYEON', '냉면', '차가운 육수나 매콤한 양념으로 먹는 시원한 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('GIMBAP', '김밥', '밥과 여러 재료를 김으로 말아 먹는 간편한 분식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('TTEOKBOKKI', '떡볶이', '떡과 어묵을 매콤달콤한 양념에 끓인 분식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('UDON', '우동', '굵은 면과 따뜻한 국물을 함께 먹는 일식 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('SOBA', '소바', '메밀면을 차갑거나 따뜻한 국물에 곁들이는 일식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('GYUDON', '규동', '밥 위에 달콤짭짤하게 조린 소고기를 올린 일본식 덮밥입니다.', b'1', NOW(6), NOW(6)),
    ('KARAAGE', '가라아게', '닭고기를 바삭하게 튀긴 일본식 튀김 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('MAPO_TOFU', '마파두부', '두부와 다진 고기를 매콤한 소스에 볶은 중식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('TANGSUYUK', '탕수육', '튀긴 고기에 새콤달콤한 소스를 곁들이는 중식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('FRIED_RICE', '볶음밥', '밥과 채소, 고기 또는 해산물을 함께 볶은 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('MALA_XIANG_GUO', '마라샹궈', '다양한 재료를 마라 양념에 볶아 먹는 중식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('PASTA', '파스타', '면과 소스를 중심으로 즐기는 대표적인 양식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('PIZZA', '피자', '도우 위에 토핑과 치즈를 올려 구운 양식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('HAMBURGER', '햄버거', '빵 사이에 패티와 채소, 소스를 넣은 패스트푸드 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('SALAD', '샐러드', '신선한 채소와 드레싱을 중심으로 가볍게 먹는 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('STEAK', '스테이크', '고기를 두툼하게 구워 소스와 곁들이는 양식 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('PHO', '쌀국수', '쌀면과 향신료가 들어간 국물을 함께 먹는 베트남식 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('PAD_THAI', '팟타이', '쌀국수와 새우, 채소를 새콤달콤하게 볶은 태국식 면 요리입니다.', b'1', NOW(6), NOW(6)),
    ('CURRY_RICE', '카레라이스', '밥에 진한 카레 소스를 얹어 먹는 메뉴입니다.', b'1', NOW(6), NOW(6)),
    ('BANH_MI', '반미', '바게트에 고기와 채소를 넣어 먹는 베트남식 샌드위치입니다.', b'1', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    code = VALUES(code);

INSERT INTO menu_attribute_categories (menu_id, attribute_category_id, created_at, updated_at)
SELECT menu.id, category.id, NOW(6), NOW(6)
FROM (
    SELECT 'KIMCHI_STEW' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'FLAVOR' AS category_type, 'NUTTY' AS category_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'TEXTURE' AS category_type, 'CRUNCHY' AS category_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'COOKING_METHOD' AS category_type, 'FRIED' AS category_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'COOKING_METHOD' AS category_type, 'RAW_SALAD' AS category_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'COOKING_METHOD' AS category_type, 'GRILLED' AS category_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'COOKING_METHOD' AS category_type, 'GRILLED' AS category_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'FLAVOR' AS category_type, 'NUTTY' AS category_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'BUNSIK' AS category_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'BUNSIK' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'COOKING_METHOD' AS category_type, 'FRIED' AS category_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'COOKING_METHOD' AS category_type, 'FRIED' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'WESTERN' AS category_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'COOKING_METHOD' AS category_type, 'GRILLED' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'WESTERN' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'COOKING_METHOD' AS category_type, 'GRILLED' AS category_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'FAST_FOOD' AS category_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'COOKING_METHOD' AS category_type, 'RAW_SALAD' AS category_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'WESTERN' AS category_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'TEXTURE' AS category_type, 'CRUNCHY' AS category_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'COOKING_METHOD' AS category_type, 'GRILLED' AS category_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'WESTERN' AS category_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'COOKING_METHOD' AS category_type, 'NOODLE_MIXED' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'ASIAN' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'FLAVOR' AS category_type, 'SWEET' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'ASIAN' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'TEXTURE' AS category_type, 'CHEWY' AS category_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'ASIAN' AS category_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'TEXTURE' AS category_type, 'SOFT' AS category_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'COOKING_METHOD' AS category_type, 'RAW_SALAD' AS category_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'FOOD_CATEGORY' AS category_type, 'ASIAN' AS category_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
) seed
JOIN menu_items menu ON menu.code = seed.menu_code
JOIN attribute_categories category
  ON category.category_type = seed.category_type
 AND category.code = seed.category_code
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO menu_ingredients (menu_id, ingredient_id, created_at, updated_at)
SELECT menu.id, ingredient.id, NOW(6), NOW(6)
FROM (
    SELECT 'KIMCHI_STEW' AS menu_code, 'KIMCHI' AS ingredient_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'TOFU' AS ingredient_code
    UNION ALL
    SELECT 'KIMCHI_STEW' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'TOFU' AS ingredient_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'MUSHROOM' AS ingredient_code
    UNION ALL
    SELECT 'DOENJANG_STEW' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'SESAME' AS ingredient_code
    UNION ALL
    SELECT 'BIBIMBAP' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'PORK_CUTLET' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'JJAJANGMYEON' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'SQUID' AS ingredient_code
    UNION ALL
    SELECT 'JJAMPPONG' AS menu_code, 'SHELLFISH' AS ingredient_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'RAMEN' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'FISH' AS ingredient_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'SUSHI' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'SESAME' AS ingredient_code
    UNION ALL
    SELECT 'BULGOGI' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'SAMGYEOPSAL' AS menu_code, 'SESAME' AS ingredient_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'CHICKEN' AS ingredient_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'DAK_GALBI' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'KIMCHI' AS ingredient_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'BUDAE_JJIGAE' AS menu_code, 'CHEESE' AS ingredient_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'GALBITANG' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'BUCKWHEAT' AS ingredient_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'NAENGMYEON' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'GIMBAP' AS menu_code, 'SESAME' AS ingredient_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'TTEOKBOKKI' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'UDON' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'BUCKWHEAT' AS ingredient_code
    UNION ALL
    SELECT 'SOBA' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'GYUDON' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'CHICKEN' AS ingredient_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'KARAAGE' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'TOFU' AS ingredient_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'MAPO_TOFU' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'TANGSUYUK' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'FRIED_RICE' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'MALA_XIANG_GUO' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'MILK' AS ingredient_code
    UNION ALL
    SELECT 'PASTA' AS menu_code, 'CHEESE' AS ingredient_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'MILK' AS ingredient_code
    UNION ALL
    SELECT 'PIZZA' AS menu_code, 'CHEESE' AS ingredient_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'CHEESE' AS ingredient_code
    UNION ALL
    SELECT 'HAMBURGER' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'SALAD' AS menu_code, 'CHEESE' AS ingredient_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'STEAK' AS menu_code, 'GARLIC' AS ingredient_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'PHO' AS menu_code, 'CILANTRO' AS ingredient_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'PEANUT' AS ingredient_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'EGG' AS ingredient_code
    UNION ALL
    SELECT 'PAD_THAI' AS menu_code, 'SOYBEAN' AS ingredient_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'BEEF' AS ingredient_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'CURRY_RICE' AS menu_code, 'MILK' AS ingredient_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'WHEAT' AS ingredient_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'BANH_MI' AS menu_code, 'CILANTRO' AS ingredient_code
) seed
JOIN menu_items menu ON menu.code = seed.menu_code
JOIN ingredients ingredient ON ingredient.code = seed.ingredient_code
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

-- Local sample members
INSERT INTO members (login_id, password_hash, nickname, nickname_completed, email, is_social, social_provider_type, social_provider_user_id, member_role, status, created_at, updated_at) VALUES
    ('tester01', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터일', b'1', 'tester01@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
    ('tester02', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터이', b'1', 'tester02@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
    ('admin01', '$2a$10$y9lgSzbq2RGzj22DWiXSq.JM.dYmo6t7MTZJABIWhblVc9OBX7V76', 'matchuri-admin', b'1', 'admin01@example.com', b'0', NULL, NULL, 'ADMIN', 'ACTIVE', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE
    nickname_completed = VALUES(nickname_completed),
    member_role = VALUES(member_role),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

INSERT INTO member_agreements (member_id, agreement_type, agreement_version, agreed_at, created_at, updated_at)
SELECT member.id, agreement.agreement_type, agreement.agreement_version, NOW(6), NOW(6), NOW(6)
FROM members member
JOIN (
    SELECT 'TERMS_OF_SERVICE' AS agreement_type, '2026-04-10' AS agreement_version
    UNION ALL
    SELECT 'PRIVACY_POLICY' AS agreement_type, '2026-04-10' AS agreement_version
) agreement
WHERE member.login_id IN ('tester01', 'tester02', 'admin01')
ON DUPLICATE KEY UPDATE agreement_version = VALUES(agreement_version);
