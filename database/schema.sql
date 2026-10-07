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

CREATE TABLE IF NOT EXISTS workshops (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  legal_name VARCHAR(220) NOT NULL,
  rfc VARCHAR(13) NOT NULL,
  phone VARCHAR(10) NOT NULL,
  email VARCHAR(180) NOT NULL,
  street VARCHAR(160) NOT NULL,
  neighborhood VARCHAR(120) NOT NULL,
  municipality VARCHAR(120) NOT NULL,
  state VARCHAR(120) NOT NULL,
  postal_code CHAR(5) NOT NULL,
  photo_path VARCHAR(260) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_workshops_rfc (rfc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO workshops (name, legal_name, rfc, phone, email, street, neighborhood, municipality, state, postal_code)
VALUES
  ('tula', 'taller mecanico tula sa de cv', 'TMT260101A1B', '7731000001', 'tula@taller.local', 'av. principal 100', 'centro', 'tula de allende', 'hidalgo', '42800'),
  ('tepeji', 'taller mecanico tepeji sa de cv', 'TMT260102A1B', '7731000002', 'tepeji@taller.local', 'av. industrial 200', 'centro', 'tepeji del rio de ocampo', 'hidalgo', '42850'),
  ('queretaro', 'taller mecanico queretaro sa de cv', 'TMQ260103A1B', '4421000003', 'queretaro@taller.local', 'av. constituyentes 300', 'centro', 'queretaro', 'queretaro', '76000')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  legal_name = VALUES(legal_name),
  phone = VALUES(phone),
  email = VALUES(email),
  street = VALUES(street),
  neighborhood = VALUES(neighborhood),
  municipality = VALUES(municipality),
  state = VALUES(state),
  postal_code = VALUES(postal_code);

CREATE TABLE IF NOT EXISTS customers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  second_last_name VARCHAR(80) NOT NULL,
  alternate_contact_name VARCHAR(160) NOT NULL,
  birth_date DATE NOT NULL,
  curp VARCHAR(18) NOT NULL,
  rfc VARCHAR(13) NOT NULL,
  contact_phone VARCHAR(10) NOT NULL,
  work_phone VARCHAR(10) NOT NULL,
  email VARCHAR(180) NOT NULL,
  work_email VARCHAR(180) NULL,
  street VARCHAR(160) NOT NULL,
  neighborhood VARCHAR(120) NOT NULL,
  municipality VARCHAR(120) NOT NULL,
  state VARCHAR(120) NOT NULL,
  postal_code CHAR(5) NOT NULL,
  photo_path VARCHAR(260) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customers_email (email),
  UNIQUE KEY uk_customers_curp (curp),
  UNIQUE KEY uk_customers_rfc (rfc),
  KEY idx_customers_created_by_user_id (created_by_user_id),
  KEY idx_customers_status (status),
  CONSTRAINT fk_customers_created_by_user
    FOREIGN KEY (created_by_user_id) REFERENCES users (id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_workshop (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  workshop_id BIGINT NOT NULL,
  first_visit_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_visit_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  registered_by_user_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_workshop_pair (customer_id, workshop_id),
  KEY idx_customer_workshop_customer (customer_id),
  KEY idx_customer_workshop_workshop (workshop_id),
  KEY idx_customer_workshop_registered_by (registered_by_user_id),
  CONSTRAINT fk_customer_workshop_customer
    FOREIGN KEY (customer_id) REFERENCES customers (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_customer_workshop_workshop
    FOREIGN KEY (workshop_id) REFERENCES workshops (id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_customer_workshop_registered_by
    FOREIGN KEY (registered_by_user_id) REFERENCES users (id)
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
  KEY idx_postal_settlements_state_municipality (state_name, municipality_name),
  KEY idx_postal_settlements_full_address (state_name, municipality_name, settlement_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migracion fase 03 para instalaciones con customers.workshop_id y columnas antiguas.
-- Ejecuta este bloque solo si tu base ya tenia datos antes de esta fase.
DELIMITER $$
CREATE PROCEDURE migrate_phase03_customers()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'full_name'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'first_name'
    ) THEN
      ALTER TABLE customers ADD COLUMN first_name VARCHAR(80) NOT NULL DEFAULT 'pendiente';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'last_name'
    ) THEN
      ALTER TABLE customers ADD COLUMN last_name VARCHAR(80) NOT NULL DEFAULT 'pendiente';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'second_last_name'
    ) THEN
      ALTER TABLE customers ADD COLUMN second_last_name VARCHAR(80) NOT NULL DEFAULT 'pendiente';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'curp'
    ) THEN
      ALTER TABLE customers ADD COLUMN curp VARCHAR(18) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'rfc'
    ) THEN
      ALTER TABLE customers ADD COLUMN rfc VARCHAR(13) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'contact_phone'
    ) THEN
      ALTER TABLE customers ADD COLUMN contact_phone VARCHAR(10) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'status'
    ) THEN
      ALTER TABLE customers ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'photo_path'
    ) THEN
      ALTER TABLE customers ADD COLUMN photo_path VARCHAR(260) NULL;
    END IF;

    UPDATE customers
    SET
      first_name = COALESCE(NULLIF(first_name, ''), LOWER(SUBSTRING_INDEX(full_name, ' ', 1))),
      last_name = COALESCE(NULLIF(last_name, ''), 'pendiente'),
      second_last_name = COALESCE(NULLIF(second_last_name, ''), 'pendiente'),
      curp = COALESCE(curp, CONCAT('XEXX010101HNEXXX', LPAD(CONV(id, 10, 36), 2, '0'))),
      rfc = COALESCE(rfc, CONCAT('XEXX010101', LPAD(MOD(id, 1000), 3, '0'))),
      contact_phone = COALESCE(contact_phone, RIGHT(LPAD(REGEXP_REPLACE(personal_phone, '[^0-9]', ''), 10, '0'), 10)),
      postal_code = RIGHT(LPAD(LEFT(REGEXP_REPLACE(postal_code, '[^0-9]', ''), 5), 5, '0'), 5)
    WHERE full_name IS NOT NULL;

    IF EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'uk_customers_personal_phone'
    ) THEN
      ALTER TABLE customers DROP INDEX uk_customers_personal_phone;
    END IF;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'full_name'
    ) THEN
      ALTER TABLE customers DROP COLUMN full_name;
    END IF;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'age'
    ) THEN
      ALTER TABLE customers DROP COLUMN age;
    END IF;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'personal_phone'
    ) THEN
      ALTER TABLE customers DROP COLUMN personal_phone;
    END IF;
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'photo_data_url'
    ) THEN
      ALTER TABLE customers DROP COLUMN photo_data_url;
    END IF;
    ALTER TABLE customers MODIFY curp VARCHAR(18) NOT NULL;
    ALTER TABLE customers MODIFY rfc VARCHAR(13) NOT NULL;
    ALTER TABLE customers MODIFY contact_phone VARCHAR(10) NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'workshop_id'
  ) THEN
    INSERT IGNORE INTO customer_workshop (customer_id, workshop_id, first_visit_at, last_visit_at, registered_by_user_id)
    SELECT
      c.id,
      COALESCE(c.workshop_id, (SELECT id FROM workshops WHERE rfc = 'TMT260101A1B' LIMIT 1)),
      COALESCE(c.created_at, CURRENT_TIMESTAMP),
      COALESCE(c.created_at, CURRENT_TIMESTAMP),
      c.created_by_user_id
    FROM customers c;

    IF EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'idx_customers_workshop_id'
    ) THEN
      ALTER TABLE customers DROP INDEX idx_customers_workshop_id;
    END IF;
    ALTER TABLE customers DROP COLUMN workshop_id;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'uk_customers_curp'
  ) THEN
    ALTER TABLE customers ADD UNIQUE KEY uk_customers_curp (curp);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'uk_customers_rfc'
  ) THEN
    ALTER TABLE customers ADD UNIQUE KEY uk_customers_rfc (rfc);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'idx_customers_status'
  ) THEN
    ALTER TABLE customers ADD KEY idx_customers_status (status);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'photo_path'
  ) THEN
    ALTER TABLE customers ADD COLUMN photo_path VARCHAR(260) NULL;
  END IF;
END$$
DELIMITER ;

CALL migrate_phase03_customers();
DROP PROCEDURE migrate_phase03_customers;

-- Las contrasenas se guardan con BCrypt: hash + salt unico por password.
-- El catalogo postal se carga de forma local desde database/sepomex_data.sql.
-- Las fotos de talleres se guardan en disco/volumen mediante UPLOAD_DIR.
-- El contenedor crea la base de datos y el usuario de aplicacion desde .env.
