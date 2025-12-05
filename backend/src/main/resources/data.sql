-- Reset tables
TRUNCATE TABLE nutrition_plan RESTART IDENTITY CASCADE;
TRUNCATE TABLE client RESTART IDENTITY CASCADE;
TRUNCATE TABLE coach RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-----------------------------------------
-- USERS
-----------------------------------------

INSERT INTO users (id, email, password, role, created_at, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'admin-1@omni.one',  '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'ADMIN',  '2025-12-01', true),
    ('00000000-0000-0000-0000-000000000010', 'coach-10@omni.one', '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'COACH', '2025-12-01', true),
    ('00000000-0000-0000-0000-000000000011', 'coach-11@omni.one', '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'COACH', '2025-12-01', true),
    ('00000000-0000-0000-0000-000000000100', 'client-100@omni.one', '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'CLIENT', '2025-12-01', true),
    ('00000000-0000-0000-0000-000000000101', 'client-101@omni.one', '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'CLIENT', '2025-12-01', true),
    ('00000000-0000-0000-0000-000000000102', 'client-102@omni.one', '$2a$12$eZQ5iHOC5gmrYD77UBqpm.YSXKHnAyQ1jtP3EA1bfy1rMUYf9w8MW', 'CLIENT', '2025-12-01', true);


-----------------------------------------
-- COACHES  (IDs discovered via email)
-----------------------------------------

INSERT INTO coach (id)
SELECT id FROM users WHERE role = 'COACH';


-----------------------------------------
-- CLIENTS  (link to coaches via email lookup)
-----------------------------------------

INSERT INTO client (id, status, coach_id)
VALUES
    (
        (SELECT id FROM users WHERE email = 'client-100@omni.one'),
        'PENDING',
        (SELECT id FROM users WHERE email = 'coach-10@omni.one')
    ),
    (
        (SELECT id FROM users WHERE email = 'client-101@omni.one'),
        'PENDING',
        (SELECT id FROM users WHERE email = 'coach-10@omni.one')
    ),
    (
        (SELECT id FROM users WHERE email = 'client-102@omni.one'),
        'PENDING',
        (SELECT id FROM users WHERE email = 'coach-11@omni.one')
    );


-----------------------------------------
-- NUTRITION PLANS (link via client email)
-----------------------------------------

-- Client 100
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2400, 300, 120, 80, '2024-01-01', NULL, (SELECT id FROM users WHERE email = 'client-100@omni.one')),
    (2200, 250, 110, 70, '2023-09-01', '2023-12-31', (SELECT id FROM users WHERE email = 'client-100@omni.one'));

-- Client 101
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (1800, 200, 90, 60, '2023-01-01', '2023-03-01', (SELECT id FROM users WHERE email = 'client-101@omni.one'));

-- Client 102
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2600, 310, 130, 90, '2024-02-15', NULL, (SELECT id FROM users WHERE email = 'client-102@omni.one')),
    (2400, 280, 115, 85, '2023-05-01', '2023-11-01', (SELECT id FROM users WHERE email = 'client-102@omni.one'));
