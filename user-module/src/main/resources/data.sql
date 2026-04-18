-- Добавление тестовых пользователей
INSERT INTO users (id, email, name, active) VALUES
('10000000-0000-0000-0000-000000000001', 'ivan.ivanov@example.com', 'Иван Иванов', true),
('10000000-0000-0000-0000-000000000002', 'petr.petrov@example.com', 'Петр Петров', true),
('10000000-0000-0000-0000-000000000003', 'anna.smirnova@example.com', 'Анна Смирнова', true),
('10000000-0000-0000-0000-000000000004', 'alexey.kuznetsov@example.com', 'Алексей Кузнецов', true),
('10000000-0000-0000-0000-000000000005', 'maria.popova@example.com', 'Мария Попова', true),
('10000000-0000-0000-0000-000000000099', 'inactive.user@example.com', 'Неактивный Пользователь', false);