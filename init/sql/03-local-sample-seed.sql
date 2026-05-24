SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- Local sample members
INSERT INTO members (login_id, password_hash, nickname, nickname_completed, email, is_social, social_provider_type, social_provider_user_id, member_role, status, created_at, updated_at) VALUES
    ('tester01', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터일', b'1', 'tester01@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
    ('tester02', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터이', b'1', 'tester02@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
    ('tester03', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터삼', b'1', 'tester03@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
    ('tester04', '$2a$10$VVuO/Z4eCysA.tIaAEnjkOwHhT42Kx1ci84jB5yNR8td9fkO7wNGq', '테스터사', b'1', 'tester04@example.com', b'0', NULL, NULL, 'MEMBER', 'ACTIVE', NOW(6), NOW(6)),
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
WHERE member.login_id IN ('tester01', 'tester02', 'tester03', 'tester04', 'admin01')
ON DUPLICATE KEY UPDATE agreement_version = VALUES(agreement_version);

-- Local sample taste profiles. Existing profile details are not deleted; only missing sample rows are added.
INSERT INTO member_taste_profiles (member_id, profile_version, created_at, updated_at)
SELECT member.id, 'v1', NOW(6), NOW(6)
FROM members member
WHERE member.login_id IN ('tester01', 'tester02', 'tester03', 'tester04')
ON DUPLICATE KEY UPDATE member_id = VALUES(member_id);

INSERT INTO member_taste_profile_categories (profile_id, attribute_category_id, created_at, updated_at)
SELECT profile.id, category.id, NOW(6), NOW(6)
FROM (
    SELECT 'tester01' AS login_id, 'FOOD_CATEGORY' AS category_type, 'KOREAN' AS category_code
    UNION ALL
    SELECT 'tester01' AS login_id, 'COOKING_METHOD' AS category_type, 'SOUP' AS category_code
    UNION ALL
    SELECT 'tester01' AS login_id, 'FLAVOR' AS category_type, 'SPICY' AS category_code
    UNION ALL
    SELECT 'tester01' AS login_id, 'TEMPERATURE' AS category_type, 'HOT' AS category_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'FOOD_CATEGORY' AS category_type, 'JAPANESE' AS category_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'COOKING_METHOD' AS category_type, 'FRIED' AS category_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'TEXTURE' AS category_type, 'CRISPY' AS category_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'FLAVOR' AS category_type, 'SALTY' AS category_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'FOOD_CATEGORY' AS category_type, 'WESTERN' AS category_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'COOKING_METHOD' AS category_type, 'RAW_SALAD' AS category_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'FLAVOR' AS category_type, 'FRESH' AS category_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'TEMPERATURE' AS category_type, 'COLD' AS category_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'FOOD_CATEGORY' AS category_type, 'CHINESE' AS category_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'COOKING_METHOD' AS category_type, 'STIR_FRIED' AS category_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'FLAVOR' AS category_type, 'RICH' AS category_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'FLAVOR' AS category_type, 'SPICY' AS category_code
) seed
JOIN members member ON member.login_id = seed.login_id
JOIN member_taste_profiles profile ON profile.member_id = member.id
JOIN attribute_categories category
  ON category.category_type = seed.category_type
 AND category.code = seed.category_code
ON DUPLICATE KEY UPDATE profile_id = VALUES(profile_id);

INSERT INTO member_taste_profile_restriction_ingredients (profile_id, ingredient_id, created_at, updated_at)
SELECT profile.id, ingredient.id, NOW(6), NOW(6)
FROM (
    SELECT 'tester01' AS login_id, 'CILANTRO' AS ingredient_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'SHRIMP' AS ingredient_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'PORK' AS ingredient_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'MILK' AS ingredient_code
) seed
JOIN members member ON member.login_id = seed.login_id
JOIN member_taste_profiles profile ON profile.member_id = member.id
JOIN ingredients ingredient ON ingredient.code = seed.ingredient_code
ON DUPLICATE KEY UPDATE profile_id = VALUES(profile_id);

INSERT INTO member_taste_profile_disliked_menu_items (profile_id, menu_id, created_at, updated_at)
SELECT profile.id, menu.id, NOW(6), NOW(6)
FROM (
    SELECT 'tester01' AS login_id, 'SALAD' AS menu_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'JJAMPPONG' AS menu_code
    UNION ALL
    SELECT 'tester02' AS login_id, 'PAD_THAI' AS menu_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'BUDAE_JJIGAE' AS menu_code
    UNION ALL
    SELECT 'tester03' AS login_id, 'SAMGYEOPSAL' AS menu_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'PIZZA' AS menu_code
    UNION ALL
    SELECT 'tester04' AS login_id, 'PASTA' AS menu_code
) seed
JOIN members member ON member.login_id = seed.login_id
JOIN member_taste_profiles profile ON profile.member_id = member.id
JOIN menu_items menu ON menu.code = seed.menu_code
ON DUPLICATE KEY UPDATE profile_id = VALUES(profile_id);

-- Local sample groups for group recommendation flows
INSERT INTO group_rooms (name, invite_code, host_member_id, latitude, longitude, status, created_at, updated_at)
SELECT seed.name, seed.invite_code, host.id, seed.latitude, seed.longitude, 'ACTIVE', NOW(6), NOW(6)
FROM (
    SELECT '점심 결정 A팀' AS name, 'LUNCHA2026' AS invite_code, 'tester01' AS host_login_id, 37.5665000 AS latitude, 126.9780000 AS longitude
    UNION ALL
    SELECT '매운맛 탐험대' AS name, 'SPICY2026' AS invite_code, 'tester04' AS host_login_id, 37.4979000 AS latitude, 127.0276000 AS longitude
) seed
JOIN members host ON host.login_id = seed.host_login_id
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    host_member_id = VALUES(host_member_id),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

INSERT INTO group_room_members (room_id, member_id, role, status, joined_at, left_at, created_at, updated_at)
SELECT room.id, member.id, seed.role, 'ACTIVE', NOW(6), NULL, NOW(6), NOW(6)
FROM (
    SELECT 'LUNCHA2026' AS invite_code, 'tester01' AS login_id, 'OWNER' AS role
    UNION ALL
    SELECT 'LUNCHA2026' AS invite_code, 'tester02' AS login_id, 'MEMBER' AS role
    UNION ALL
    SELECT 'LUNCHA2026' AS invite_code, 'tester03' AS login_id, 'MEMBER' AS role
    UNION ALL
    SELECT 'SPICY2026' AS invite_code, 'tester04' AS login_id, 'OWNER' AS role
    UNION ALL
    SELECT 'SPICY2026' AS invite_code, 'tester01' AS login_id, 'MEMBER' AS role
) seed
JOIN group_rooms room ON room.invite_code = seed.invite_code
JOIN members member ON member.login_id = seed.login_id
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    status = VALUES(status),
    left_at = VALUES(left_at),
    updated_at = VALUES(updated_at);
