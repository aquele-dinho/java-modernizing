-- Sample users (passwords are BCrypt encoded)
-- Password for both users: "password"
INSERT INTO users (id, username, email, password, roles, created_at) VALUES
(1, 'admin', 'admin@demo.com', '$2y$05$4haVnHmVRaXcTwn8CSVijulJu2NGU4Vzs031HDPbBFZoFM4.IoKWm', 'USER,ADMIN', CURRENT_TIMESTAMP),
(2, 'user', 'user@demo.com', '$2y$05$4haVnHmVRaXcTwn8CSVijulJu2NGU4Vzs031HDPbBFZoFM4.IoKWm', 'USER', CURRENT_TIMESTAMP);

-- Sample tasks
INSERT INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) VALUES
(1, 'Setup development environment', 'Install Java, Maven, and IDE for the project', 'COMPLETED', 'HIGH', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Implement user authentication', 'Create JWT-based authentication system', 'IN_PROGRESS', 'HIGH', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Write unit tests', 'Add comprehensive unit tests for all services', 'OPEN', 'MEDIUM', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'API documentation', 'Document all REST endpoints with examples', 'OPEN', 'LOW', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Code review', 'Review pull requests from team members', 'OPEN', 'MEDIUM', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
