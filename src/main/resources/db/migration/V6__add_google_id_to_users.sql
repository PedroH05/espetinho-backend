ALTER TABLE users
ADD COLUMN google_id VARCHAR(120);

CREATE UNIQUE INDEX idx_users_google_id ON users (google_id)
WHERE google_id IS NOT NULL;
