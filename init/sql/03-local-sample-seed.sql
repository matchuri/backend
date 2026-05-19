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
