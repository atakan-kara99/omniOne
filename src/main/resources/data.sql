TRUNCATE TABLE nutrition_plan RESTART IDENTITY CASCADE;
TRUNCATE TABLE client RESTART IDENTITY CASCADE;
TRUNCATE TABLE coach RESTART IDENTITY CASCADE;

-----------------------------------------
-- COACHES
-----------------------------------------
INSERT INTO coach (email)
VALUES
    ('coach.alex@example.com'),
    ('coach.sarah@example.com');

-----------------------------------------
-- CLIENTS
-----------------------------------------
INSERT INTO client (email, status, coach_id)
VALUES
    ('client.doe@example.com', 'PENDING', 1),
    ('client.smith@example.com', 'PENDING', 1),
    ('client.brown@example.com', 'PENDING', 2);

-----------------------------------------
-- NUTRITION PLANS
-----------------------------------------

-- Client 1
-- Active plan (endDate = NULL)
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2400, 300, 120, 80, '2024-01-01', NULL, 1);
-- Past plan
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2200, 250, 110, 70, '2023-09-01', '2023-12-31', 1);


-- Client 2
-- One old plan
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (1800, 200, 90, 60, '2023-01-01', '2023-03-01', 2);


-- Client 3
-- Active plan
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2600, 310, 130, 90, '2024-02-15', NULL, 3);
-- Old plan
INSERT INTO nutrition_plan (calories, carbohydrates, proteins, fats, start_date, end_date, client_id)
VALUES
    (2400, 280, 115, 85, '2023-05-01', '2023-11-01', 3);
