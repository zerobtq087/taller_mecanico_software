CREATE DATABASE IF NOT EXISTS taller_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'taller_app'@'%' IDENTIFIED BY 'TallerApp87!';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON taller_db.* TO 'taller_app'@'%';
FLUSH PRIVILEGES;

USE taller_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  password_reset_token VARCHAR(96) NULL,
  password_reset_expires_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_reset_token (password_reset_token)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(40) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Las contrasenas se guardan con BCrypt: hash + salt unico por password.
-- Usuario demo: gerente@taller.local / Paradox87!
INSERT INTO users (name, email, password_hash, enabled)
VALUES ('Gerente Demo', 'gerente@taller.local', '$2a$12$u5pMZiPsPC3YxBnlKaY7V.WhYe3g3Wxcgjt0eITtQNvF64tjgqLh.', TRUE)
ON DUPLICATE KEY UPDATE email = email;

INSERT IGNORE INTO user_roles (user_id, role)
SELECT id, 'GERENTE' FROM users WHERE email = 'gerente@taller.local';
