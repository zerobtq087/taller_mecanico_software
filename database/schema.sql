USE taller_db;

ALTER DATABASE taller_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(40) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  full_name VARCHAR(160) NOT NULL,
  alternate_contact_name VARCHAR(160) NOT NULL,
  age INT NOT NULL,
  birth_date DATE NOT NULL,
  personal_phone VARCHAR(25) NOT NULL,
  work_phone VARCHAR(25) NOT NULL,
  email VARCHAR(180) NOT NULL,
  work_email VARCHAR(180) NULL,
  photo_data_url LONGTEXT NULL,
  street VARCHAR(160) NOT NULL,
  neighborhood VARCHAR(120) NOT NULL,
  municipality VARCHAR(120) NOT NULL,
  state VARCHAR(120) NOT NULL,
  postal_code VARCHAR(12) NOT NULL,
  workshop_id BIGINT NULL,
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customers_email (email),
  UNIQUE KEY uk_customers_personal_phone (personal_phone),
  KEY idx_customers_workshop_id (workshop_id),
  KEY idx_customers_created_by_user_id (created_by_user_id),
  CONSTRAINT fk_customers_created_by_user
    FOREIGN KEY (created_by_user_id) REFERENCES users (id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS postal_settlements (
  id BIGINT NOT NULL,
  source_settlement_id BIGINT NOT NULL,
  postal_code CHAR(5) NOT NULL,
  settlement_name VARCHAR(180) NOT NULL,
  settlement_type VARCHAR(80) NOT NULL,
  municipality_name VARCHAR(140) NOT NULL,
  state_name VARCHAR(140) NOT NULL,
  city_name VARCHAR(140) NULL,
  municipal_identifier VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_postal_settlement_source (source_settlement_id),
  KEY idx_postal_settlements_postal_code (postal_code),
  KEY idx_postal_settlements_state_municipality (state_name, municipality_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Las contrasenas se guardan con BCrypt: hash + salt unico por password.
-- El catalogo postal se carga de forma local desde database/sepomex_data.sql.
-- El contenedor crea la base de datos y el usuario de aplicacion desde .env.
