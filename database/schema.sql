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

CREATE TABLE IF NOT EXISTS status_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  str_valor VARCHAR(40) NOT NULL,
  str_descripcion VARCHAR(180) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_status_catalog_str_valor (str_valor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO status_catalog (str_valor, str_descripcion)
VALUES
  ('ACTIVO', 'activo'),
  ('SUSPENDIDO', 'suspendido'),
  ('CANCELADO', 'cancelado'),
  ('ACTUALIZADO', 'actualizado')
ON DUPLICATE KEY UPDATE str_descripcion = VALUES(str_descripcion);

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
  status_id BIGINT NOT NULL DEFAULT 1,
  created_by_user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customers_email (email),
  UNIQUE KEY uk_customers_curp (curp),
  UNIQUE KEY uk_customers_rfc (rfc),
  KEY idx_customers_created_by_user_id (created_by_user_id),
  KEY idx_customers_status_id (status_id),
  CONSTRAINT fk_customers_created_by_user
    FOREIGN KEY (created_by_user_id) REFERENCES users (id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_customers_status
    FOREIGN KEY (status_id) REFERENCES status_catalog (id)
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

CREATE TABLE IF NOT EXISTS vehicle_makes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  make_group VARCHAR(120) NULL,
  name VARCHAR(120) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicle_makes_name (name),
  KEY idx_vehicle_makes_group (make_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicle_models (
  id BIGINT NOT NULL AUTO_INCREMENT,
  make_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  year_start INT NULL,
  year_end INT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicle_models_make_name (make_id, name),
  KEY idx_vehicle_models_name (name),
  CONSTRAINT fk_vehicle_models_make
    FOREIGN KEY (make_id) REFERENCES vehicle_makes (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicle_versions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  model_id BIGINT NOT NULL,
  name VARCHAR(260) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicle_versions_model_name (model_id, name),
  KEY idx_vehicle_versions_name (name),
  CONSTRAINT fk_vehicle_versions_model
    FOREIGN KEY (model_id) REFERENCES vehicle_models (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  vin VARCHAR(17) NOT NULL,
  plate VARCHAR(12) NOT NULL,
  make VARCHAR(120) NOT NULL,
  model VARCHAR(160) NOT NULL,
  model_year INT NOT NULL,
  version VARCHAR(260) NOT NULL,
  color VARCHAR(80) NOT NULL,
  mileage INT NULL,
  serial_number VARCHAR(80) NOT NULL,
  status_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicles_vin (vin),
  UNIQUE KEY uk_vehicles_plate (plate),
  KEY idx_vehicles_customer (customer_id),
  KEY idx_vehicles_status (status_id),
  CONSTRAINT fk_vehicles_customer
    FOREIGN KEY (customer_id) REFERENCES customers (id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_vehicles_status
    FOREIGN KEY (status_id) REFERENCES status_catalog (id)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migracion fase 03 para instalaciones con customers.workshop_id y columnas antiguas.
-- Ejecuta este bloque solo si tu base ya tenia datos antes de esta fase.
DROP PROCEDURE IF EXISTS migrate_phase03_customers;
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
	    SELECT 1 FROM information_schema.columns
	    WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'photo_path'
	  ) THEN
	    ALTER TABLE customers ADD COLUMN photo_path VARCHAR(260) NULL;
	  END IF;
	  IF EXISTS (
	    SELECT 1 FROM information_schema.columns
	    WHERE table_schema = DATABASE() AND table_name = 'vehicles' AND column_name = 'version' AND character_maximum_length < 260
	  ) THEN
	    ALTER TABLE vehicles MODIFY version VARCHAR(260) NOT NULL;
	  END IF;
	  IF NOT EXISTS (
	    SELECT 1 FROM information_schema.columns
	    WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'status_id'
	  ) THEN
	    ALTER TABLE customers ADD COLUMN status_id BIGINT NULL;
	    UPDATE customers c
	    LEFT JOIN status_catalog s ON s.str_valor = COALESCE(c.status, 'ACTIVO')
	    SET c.status_id = COALESCE(s.id, (SELECT id FROM status_catalog WHERE str_valor = 'ACTIVO' LIMIT 1));
	    ALTER TABLE customers MODIFY status_id BIGINT NOT NULL;
	    ALTER TABLE customers ADD CONSTRAINT fk_customers_status
	      FOREIGN KEY (status_id) REFERENCES status_catalog (id)
	      ON DELETE RESTRICT;
	  END IF;
	  IF NOT EXISTS (
	    SELECT 1 FROM information_schema.statistics
	    WHERE table_schema = DATABASE() AND table_name = 'customers' AND index_name = 'idx_customers_status_id'
	  ) THEN
	    ALTER TABLE customers ADD KEY idx_customers_status_id (status_id);
	  END IF;
END$$
DELIMITER ;

CALL migrate_phase03_customers();
DROP PROCEDURE migrate_phase03_customers;

-- Las contrasenas se guardan con BCrypt: hash + salt unico por password.
-- El catalogo postal se carga de forma local desde database/sepomex_data.sql.
-- El catalogo de marcas, modelos y versiones se carga desde database/vehicle_catalog/vehicle_catalog_seed.sql.
-- Fuente local: makes-models.csv y engines.csv de https://github.com/gor3a/vehicle-makes-models, datos bajo ODbL 1.0.
-- Las fotos de talleres se guardan en disco/volumen mediante UPLOAD_DIR.
-- El contenedor crea la base de datos y el usuario de aplicacion desde .env.
