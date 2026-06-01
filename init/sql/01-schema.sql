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

CREATE TABLE image_assets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    storage_provider VARCHAR(30) NOT NULL,
    bucket VARCHAR(100) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    original_filename VARCHAR(255),
    content_type VARCHAR(50) NOT NULL,
    content_length BIGINT NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_image_assets_object_key UNIQUE (object_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_item_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    image_asset_id BIGINT NOT NULL,
    image_role VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL,
    is_primary BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_menu_item_images_menu UNIQUE (menu_id),
    INDEX idx_menu_item_images_image_asset (image_asset_id)
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
    closed_at DATETIME(6),
    close_reason VARCHAR(30),
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
    status VARCHAR(30) NOT NULL,
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
    score DOUBLE NOT NULL,
    candidate_meta_json JSON,
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

CREATE TABLE group_menu_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_room_id BIGINT NOT NULL,
    group_recommendation_id BIGINT NOT NULL,
    actor_member_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_group_menu_action_recommendation_menu_type UNIQUE (group_recommendation_id, menu_id, action_type),
    INDEX idx_group_menu_actions_room (group_room_id),
    INDEX idx_group_menu_actions_actor_member (actor_member_id),
    INDEX idx_group_menu_actions_menu (menu_id)
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

ALTER TABLE menu_item_images
    ADD CONSTRAINT fk_menu_item_images_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id),
    ADD CONSTRAINT fk_menu_item_images_image_asset FOREIGN KEY (image_asset_id) REFERENCES image_assets (id);

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

ALTER TABLE group_menu_actions
    ADD CONSTRAINT fk_group_menu_actions_room FOREIGN KEY (group_room_id) REFERENCES group_rooms (id),
    ADD CONSTRAINT fk_group_menu_actions_recommendation FOREIGN KEY (group_recommendation_id) REFERENCES group_recommendations (id),
    ADD CONSTRAINT fk_group_menu_actions_actor_member FOREIGN KEY (actor_member_id) REFERENCES members (id),
    ADD CONSTRAINT fk_group_menu_actions_menu FOREIGN KEY (menu_id) REFERENCES menu_items (id);

ALTER TABLE group_presence_events
    ADD CONSTRAINT fk_group_presence_events_room FOREIGN KEY (room_id) REFERENCES group_rooms (id),
    ADD CONSTRAINT fk_group_presence_events_member FOREIGN KEY (member_id) REFERENCES members (id);
