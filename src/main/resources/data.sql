-- Sample users (passwords are BCrypt encoded)
-- Password for both users: "password"
-- Use H2 MERGE to make seeding idempotent across test runs
MERGE INTO users (id, username, email, password, roles, created_at) KEY(id) VALUES
(1, 'admin', 'admin@demo.com', '$2y$05$4haVnHmVRaXcTwn8CSVijulJu2NGU4Vzs031HDPbBFZoFM4.IoKWm', 'USER,ADMIN', CURRENT_TIMESTAMP);

MERGE INTO users (id, username, email, password, roles, created_at) KEY(id) VALUES
(2, 'user', 'user@demo.com', '$2y$05$4haVnHmVRaXcTwn8CSVijulJu2NGU4Vzs031HDPbBFZoFM4.IoKWm', 'USER', CURRENT_TIMESTAMP);

-- Sample tasks
MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES
(1, 'Setup development environment', 'Install Java, Maven, and IDE for the project', 'COMPLETED', 'HIGH', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES
(2, 'Implement user authentication', 'Create JWT-based authentication system', 'IN_PROGRESS', 'HIGH', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES
(3, 'Write unit tests', 'Add comprehensive unit tests for all services', 'OPEN', 'MEDIUM', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES
(4, 'API documentation', 'Document all REST endpoints with examples', 'OPEN', 'LOW', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES
(5, 'Code review', 'Review pull requests from team members', 'OPEN', 'MEDIUM', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Ensure H2 identity sequences continue after seeded IDs
-- Users: ids 1-2 are seeded, so next generated id should start at 3
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;

-- Tasks: ids 1-5 are seeded, so next generated id should start at 6
ALTER TABLE tasks ALTER COLUMN id RESTART WITH 6;
