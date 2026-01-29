-----------------------------------------
-- USER
-----------------------------------------

INSERT INTO user_ (id, email, password, role, created_at, enabled, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000001',    'admin-1@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC',  'ADMIN', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000010',   'coach-10@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC',  'COACH', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000011',   'coach-11@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC',  'COACH', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000100', 'client-100@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000101', 'client-101@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000102', 'client-102@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000012', 'coach@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'COACH', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000103', 'client@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false);

-----------------------------------------
-- USER_PROFILE
-----------------------------------------

INSERT INTO user_profile (user_id, created_at, updated_at, birth_date, first_name, last_name, gender)
VALUES
    ((SELECT id FROM user_ WHERE email =    'admin-1@omni.one'), '2025-12-01 00:00:00', NULL, '1980-05-14',   'Alex',    'Admin',   'MALE'),
    ((SELECT id FROM user_ WHERE email =   'coach-10@omni.one'), '2025-12-01 00:00:00', NULL, '1985-02-20', 'Jordan', 'Maverick',   'MALE'),
    ((SELECT id FROM user_ WHERE email =   'coach-11@omni.one'), '2025-12-01 00:00:00', NULL, '1990-11-03', 'Taylor',    'Stone', 'FEMALE'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), '2025-12-01 00:00:00', NULL, '1995-01-12',  'Casey',   'Rivera', 'FEMALE'),
    ((SELECT id FROM user_ WHERE email = 'client-101@omni.one'), '2025-12-01 00:00:00', NULL, '1998-07-28', 'Morgan',      'Lee',   'MALE'),
    ((SELECT id FROM user_ WHERE email = 'client-102@omni.one'), '2025-12-01 00:00:00', NULL, '2000-09-09',  'Riley',  'Kendall',  'OTHER');

-----------------------------------------
-- COACH  (IDs discovered via email)
-----------------------------------------

INSERT INTO coach (user_id, created_at, updated_at)
VALUES
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-11@omni.one'), '2025-12-01 00:00:00', NULL);

-----------------------------------------
-- CLIENT  (link to coaches via email lookup)
-----------------------------------------

INSERT INTO client (user_id, coach_id, created_at, updated_at)
VALUES
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-101@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-102@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-11@omni.one'), '2025-12-01 00:00:00', NULL);

-----------------------------------------
-- COACHING
-----------------------------------------

INSERT INTO coaching (coach_id, client_id, start_date, end_date)
VALUES
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), '2020-01-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), '2020-01-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-11@omni.one'), (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), '2020-01-01 00:00:00', NULL);

-----------------------------------------
-- NUTRITION PLAN (link via client email)
-----------------------------------------

INSERT INTO nutrition_plan (calories, carbs, proteins, fats, created_at, client_id)
VALUES
    (2400, 300, 120, 80, '2024-01-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2200, 250, 110, 70, '2023-09-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'));

INSERT INTO nutrition_plan (calories, carbs, proteins, fats, created_at, client_id)
VALUES
    (1800, 200, 90, 60, '2023-01-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'));

INSERT INTO nutrition_plan (calories, carbs, proteins, fats, created_at, client_id)
VALUES
    (2600, 310, 130, 90, '2024-02-15 00:00:00', (SELECT id FROM user_ WHERE email = 'client-102@omni.one')),
    (2400, 280, 115, 85, '2023-05-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'));

-----------------------------------------
-- QUESTIONNAIRE
-----------------------------------------

INSERT INTO questionnaire_question (coach_id, text)
VALUES
    (null,                                                     'This is my one and only standard question!'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'coach-10s custom question')
