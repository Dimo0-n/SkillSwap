INSERT INTO roles (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN'
);

INSERT INTO roles (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_USER'
);

INSERT INTO category (id, category)
SELECT 1, 'Programare' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 1);
INSERT INTO category (id, category)
SELECT 2, 'Design' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 2);
INSERT INTO category (id, category)
SELECT 3, 'Fotografie' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 3);
INSERT INTO category (id, category)
SELECT 4, 'Scriere' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 4);
INSERT INTO category (id, category)
SELECT 5, 'Marketing' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 5);
INSERT INTO category (id, category)
SELECT 6, 'Limbi straine' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 6);
INSERT INTO category (id, category)
SELECT 7, 'Business' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 7);
INSERT INTO category (id, category)
SELECT 8, 'Coaching' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 8);
INSERT INTO category (id, category)
SELECT 9, 'DIY' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 9);
INSERT INTO category (id, category)
SELECT 10, 'Arta' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 10);
INSERT INTO category (id, category)
SELECT 11, 'Muzica' WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 11);

INSERT INTO platform_setting (setting_key, label, category, setting_value, description, updated_at)
SELECT
    'allowed.file.types',
    'Allowed file types',
    'Uploads',
    'jpg,jpeg,png,webp',
    'Comma-separated whitelist used by admin moderation.',
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM platform_setting WHERE setting_key = 'allowed.file.types'
);

INSERT INTO platform_setting (setting_key, label, category, setting_value, description, updated_at)
SELECT
    'max.image.size.mb',
    'Maximum image size (MB)',
    'Uploads',
    '5',
    'Upper upload limit for profile and gallery images.',
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM platform_setting WHERE setting_key = 'max.image.size.mb'
);

INSERT INTO platform_setting (setting_key, label, category, setting_value, description, updated_at)
SELECT
    'session.duration.min',
    'Minimum session duration',
    'Sessions',
    '15',
    'Smallest allowed session duration in minutes.',
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM platform_setting WHERE setting_key = 'session.duration.min'
);

INSERT INTO platform_setting (setting_key, label, category, setting_value, description, updated_at)
SELECT
    'session.duration.max',
    'Maximum session duration',
    'Sessions',
    '180',
    'Largest allowed session duration in minutes.',
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM platform_setting WHERE setting_key = 'session.duration.max'
);

INSERT INTO platform_setting (setting_key, label, category, setting_value, description, updated_at)
SELECT
    'platform.rules',
    'Platform rules',
    'Content',
    'Be respectful. No harassment, scams, or explicit content.',
    'Rules text shown in moderation and help surfaces.',
    NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM platform_setting WHERE setting_key = 'platform.rules'
);
