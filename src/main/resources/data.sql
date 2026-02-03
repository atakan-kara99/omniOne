-----------------------------------------
-- USER
-----------------------------------------

INSERT INTO user_ (id, email, password, role, created_at, enabled, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000001',    'admin-1@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC',  'ADMIN', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000010',   'coach-10@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC',  'COACH', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000100', 'client-100@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000101', 'client-101@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000102', 'client-102@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-01T00:00:00', true, false),
    ('00000000-0000-0000-0000-000000000104', 'client-103@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-09T12:00:00', true, false),
    ('00000000-0000-0000-0000-000000000105', 'client-104@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-10T10:10:00', true, false),
    ('00000000-0000-0000-0000-000000000106', 'client-105@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-10T14:30:00', true, false),
    ('00000000-0000-0000-0000-000000000107', 'client-106@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-11T09:05:00', true, false),
    ('00000000-0000-0000-0000-000000000108', 'client-107@omni.one', '$2a$12$NNM/k7imIpzquxjfjUSh8ul9fCnH9osMeDDpzgqdFeSD/WAVyuEFC', 'CLIENT', '2025-12-11T16:40:00', true, false);

-----------------------------------------
-- USER_PROFILE
-----------------------------------------

INSERT INTO user_profile (user_id, created_at, updated_at, birth_date, first_name, last_name, gender)
VALUES
    ((SELECT id FROM user_ WHERE email =    'admin-1@omni.one'), '2025-12-01 00:00:00', NULL, '1980-05-14',   'Alex',    'Admin',   'MALE'),
    ((SELECT id FROM user_ WHERE email =   'coach-10@omni.one'), '2025-12-01 00:00:00', NULL, '1985-02-20', 'Jordan', 'Maverick',   'MALE'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), '2025-12-01 00:00:00', NULL, '1995-01-12',  'Casey',   'Rivera', 'FEMALE'),
    ((SELECT id FROM user_ WHERE email = 'client-101@omni.one'), '2025-12-01 00:00:00', NULL, '1998-07-28', 'Morgan',      'Lee',   'MALE'),
    ((SELECT id FROM user_ WHERE email = 'client-102@omni.one'), '2025-12-01 00:00:00', NULL, '2000-09-09',  'Riley',  'Kendall',  'OTHER'),
    ((SELECT id FROM user_ WHERE email = 'client-103@omni.one'), '2025-12-09 12:00:00', NULL, '1992-12-02', 'Harper', 'Mitchell', 'FEMALE'),
    ((SELECT id FROM user_ WHERE email = 'client-104@omni.one'), '2025-12-10 10:10:00', NULL, '1994-04-19',   'Rowan',   'Foster',   'MALE'),
    ((SELECT id FROM user_ WHERE email = 'client-105@omni.one'), '2025-12-10 14:30:00', NULL, '1997-08-30',   'Dakota',     'Ng',  'OTHER'),
    ((SELECT id FROM user_ WHERE email = 'client-106@omni.one'), '2025-12-11 09:05:00', NULL, '1991-02-11',   'Parker', 'Santiago',   'MALE'),
    ((SELECT id FROM user_ WHERE email = 'client-107@omni.one'), '2025-12-11 16:40:00', NULL, '1989-06-06',    'Hayes',   'Moreno', 'FEMALE');

-----------------------------------------
-- COACH  (IDs discovered via email)
-----------------------------------------

INSERT INTO coach (user_id, created_at, updated_at)
VALUES
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL);

-----------------------------------------
-- CLIENT  (link to coaches via email lookup)
-----------------------------------------

INSERT INTO client (user_id, coach_id, created_at, updated_at)
VALUES
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-101@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-102@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-103@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-09 12:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-104@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-10 10:10:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-105@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-10 14:30:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-106@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-11 09:05:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'client-107@omni.one'), (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), '2025-12-11 16:40:00', NULL);

-----------------------------------------
-- COACHING
-----------------------------------------

INSERT INTO coaching (coach_id, client_id, start_date, end_date)
VALUES
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), '2020-01-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), '2020-01-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), '2020-01-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-103@omni.one'), '2022-05-01 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-104@omni.one'), '2021-08-15 00:00:00', '2024-01-15 00:00:00'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-105@omni.one'), '2023-02-10 00:00:00', NULL),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-106@omni.one'), '2021-03-20 00:00:00', '2022-11-30 00:00:00'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), (SELECT id FROM user_ WHERE email = 'client-107@omni.one'), '2024-04-05 00:00:00', NULL);

-----------------------------------------
-- NUTRITION PLAN (link via client email)
-----------------------------------------

INSERT INTO nutrition_plan (calories, carbs, proteins, fats, water, salt, fiber, created_at, client_id)
VALUES
    (2100, 240, 105, 70, 2.6, 5.0, 28, '2024-03-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (1950, 220, 95,  65, 2.4, 4.8, 24, '2023-11-15 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2300, 270, 115, 75, 2.8, 5.5, 30, '2024-06-10 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2500, 290, 125, 85, 3.0, 6.0, 32, '2024-09-20 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2250, 260, 112, 78, 2.7, 5.4, 29, '2025-01-05 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2150, 245, 108, 72, 2.6, 5.1, 27, '2024-12-01 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2050, 235, 100, 70, 2.5, 5.0, 26, '2024-10-25 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (2350, 275, 118, 76, 2.9, 5.6, 31, '2025-03-15 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one')),
    (1900, 210, 92,  62, 2.3, 4.6, 23, '2023-08-20 00:00:00', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'));

-----------------------------------------
-- QUESTIONNAIRE
-----------------------------------------

INSERT INTO questionnaire_question (coach_id, text)
VALUES
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'This is my one and only standard question!'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'What is your primary fitness goal right now?'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'How many days per week can you train?'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'coach-10s custom question'),
    ((SELECT id FROM user_ WHERE email = 'coach-10@omni.one'), 'Any injuries or limitations I should know about?');

-----------------------------------------
-- QUESTIONNAIRE_ANSWER
-----------------------------------------

INSERT INTO questionnaire_answer (client_id, question_id, answer)
VALUES
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM questionnaire_question WHERE text = 'This is my one and only standard question!'), 'Focus on endurance and mobility.'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM questionnaire_question WHERE text = 'How many days per week can you train?'), '4'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM questionnaire_question WHERE text = 'What is your primary fitness goal right now?'), 'Lean muscle gain.'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM questionnaire_question WHERE text = 'coach-10s custom question'), 'Looking for a 12-week plan.'),
    ((SELECT id FROM user_ WHERE email = 'client-100@omni.one'), (SELECT id FROM questionnaire_question WHERE text = 'Any injuries or limitations I should know about?'), 'Mild knee pain from running.');

-----------------------------------------
-- CHAT_CONVERSATION
-----------------------------------------

INSERT INTO chat_conversation (id, last_message_preview, started_at, last_message_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'Got it — I will draft your plan by Friday.', '2025-12-02 10:00:00', '2025-12-02 10:05:00'),
    ('10000000-0000-0000-0000-000000000002', 'Let me know how the first week feels.', '2025-12-03 14:20:00', '2025-12-03 14:25:00'),
    ('10000000-0000-0000-0000-000000000003', 'We will adjust macros after your check-in.', '2025-12-04 09:00:00', '2025-12-04 09:10:00'),
    ('10000000-0000-0000-0000-000000000004', 'Welcome aboard — excited to work together!', '2025-12-09 12:05:00', '2025-12-09 12:10:00');

-----------------------------------------
-- CHAT_PARTICIPANT
-----------------------------------------

INSERT INTO chat_participant (conversation_id, user_id, joined_at, last_read_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  '2025-12-02 10:00:00', '2025-12-02 10:05:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), '2025-12-02 10:00:00', '2025-12-02 10:04:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  '2025-12-03 14:20:00', '2025-12-03 14:25:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), '2025-12-03 14:20:00', '2025-12-03 14:24:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  '2025-12-04 09:00:00', '2025-12-04 09:10:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), '2025-12-04 09:00:00', '2025-12-04 09:08:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  '2025-12-09 12:05:00', '2025-12-09 12:10:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'client-103@omni.one'), '2025-12-09 12:05:00', '2025-12-09 12:09:00');

-----------------------------------------
-- CHAT_MESSAGE
-----------------------------------------

INSERT INTO chat_message (conversation_id, sender_id, content, sent_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), 'Thanks! I will track my meals this week.', '2025-12-02 10:01:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Got it — I will draft your plan by Friday.', '2025-12-02 10:05:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), 'Week one felt good, but legs are sore.', '2025-12-03 14:22:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Let me know how the first week feels.', '2025-12-03 14:25:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), 'Check-in: weight stable and energy up.', '2025-12-04 09:05:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'We will adjust macros after your check-in.', '2025-12-04 09:10:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Welcome aboard — excited to work together!', '2025-12-09 12:10:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), 'I can train 4 days this week. Any focus?', '2025-12-02 18:30:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Let us focus on full-body and mobility.', '2025-12-02 18:34:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), 'Sleep was about 7 hours average.', '2025-12-03 08:05:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Great. Keep hydration around 2.5L.', '2025-12-03 08:10:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), 'Logged meals for 3 days so far.', '2025-12-04 19:40:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Nice. We will review on Friday.', '2025-12-04 19:42:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'client-100@omni.one'), 'Workouts were good, slight knee pain.', '2025-12-05 07:55:00'),
    ('10000000-0000-0000-0000-000000000001', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Reduce impact; add bike intervals.', '2025-12-05 08:03:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), 'Can I swap squats for leg press?', '2025-12-03 16:12:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Yes, keep the same rep range.', '2025-12-03 16:18:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), 'DOMS is intense today.', '2025-12-04 09:30:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Active recovery and light walking.', '2025-12-04 09:35:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'client-101@omni.one'), 'I will add a short stretch routine.', '2025-12-05 20:10:00'),
    ('10000000-0000-0000-0000-000000000002', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Perfect. Keep it consistent.', '2025-12-05 20:12:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), 'Macros were on target.', '2025-12-05 08:40:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Great. Increase protein by 10g.', '2025-12-05 08:45:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), 'Any tips for late-night cravings?', '2025-12-06 21:20:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Try casein or herbal tea.', '2025-12-06 21:26:00'),
    ('10000000-0000-0000-0000-000000000003', (SELECT id FROM user_ WHERE email = 'client-102@omni.one'), 'Will do. Thanks!', '2025-12-06 21:27:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'client-103@omni.one'), 'Glad to start. Goals are strength.', '2025-12-09 12:12:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'We will begin with 3 full-body days.', '2025-12-09 12:16:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'client-103@omni.one'), 'I have dumbbells and a bench.', '2025-12-09 12:18:00'),
    ('10000000-0000-0000-0000-000000000004', (SELECT id FROM user_ WHERE email = 'coach-10@omni.one'),  'Perfect. I will tailor around that.', '2025-12-09 12:20:00');
