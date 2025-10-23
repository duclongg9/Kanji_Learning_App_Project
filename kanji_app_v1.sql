/* =============================================================================
   Kanji App DB – Full Recreate Script (MySQL 8+)
   Includes: DROP DATABASE, tables, constraints, seed data, view, stored proc
   ============================================================================= */

-- 0) Nuke & recreate database
DROP DATABASE IF EXISTS `kanji_app`;
CREATE DATABASE `kanji_app`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE `kanji_app`;

-- Optional safety (keeps consistent behavior)
SET NAMES utf8mb4;
SET SQL_MODE = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

/* -----------------------------------------------------------------------------
   1) Tables
----------------------------------------------------------------------------- */

-- Roles: 1=ADMIN, 2=FREE, 3=VIP
CREATE TABLE roles (
  role_id     TINYINT      PRIMARY KEY,
  role_name   VARCHAR(20)  NOT NULL,
  description VARCHAR(200) NULL
) ENGINE=InnoDB;

-- Users (Google SSO friendly)
CREATE TABLE users (
  user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_name      VARCHAR(100) NOT NULL,
  img_url        VARCHAR(500) NULL,
  email          VARCHAR(190) NOT NULL,
  mat_khau       VARCHAR(255) NULL, -- may be NULL when using SSO
  status         ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  role_id        TINYINT NOT NULL DEFAULT 2,
  oauth_provider VARCHAR(30) NULL,          -- e.g., 'google'
  oauth_subject  VARCHAR(191) NULL,         -- 'sub' from IdToken
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_users_email UNIQUE(email),
  CONSTRAINT uq_users_oauth UNIQUE(oauth_provider, oauth_subject),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB;

-- JLPT Levels (fixed N5..N1)
CREATE TABLE cap_do_jlpt (
  cap_do_code CHAR(2) PRIMARY KEY,      -- 'N5','N4','N3','N2','N1'
  thu_tu      TINYINT NOT NULL,
  CONSTRAINT uq_cap_order UNIQUE(thu_tu)
) ENGINE=InnoDB;

-- Mức độ (e.g., 'Cơ Bản', 'Nâng Cao'...)
CREATE TABLE muc_do (
  ma_muc_do   INT AUTO_INCREMENT PRIMARY KEY,
  ten_muc_do  VARCHAR(100) NOT NULL,
  mo_ta       VARCHAR(255) NULL,
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_muc_do_name UNIQUE(ten_muc_do)
) ENGINE=InnoDB;

-- Cụm Mức độ × JLPT (FREE/VIP + bật/tắt theo cụm)
CREATE TABLE muc_do_cap_do (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  ma_muc_do     INT     NOT NULL,
  cap_do_code   CHAR(2) NOT NULL,
  access_tier   ENUM('FREE','VIP') NOT NULL DEFAULT 'FREE',
  is_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_muc_do_cap (ma_muc_do, cap_do_code),
  CONSTRAINT fk_mdc_mucdo FOREIGN KEY (ma_muc_do)   REFERENCES muc_do(ma_muc_do) ON DELETE CASCADE,
  CONSTRAINT fk_mdc_capdo FOREIGN KEY (cap_do_code) REFERENCES cap_do_jlpt(cap_do_code) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Kanji dictionary
CREATE TABLE kanji (
  ma_chu_kanji BIGINT AUTO_INCREMENT PRIMARY KEY,
  kanji        CHAR(1)      NOT NULL,
  han_viet     VARCHAR(100) NULL,
  am_on        VARCHAR(255) NULL,
  am_kun       VARCHAR(255) NULL,
  mo_ta        TEXT         NULL,
  UNIQUE KEY uq_kanji_char (kanji)
) ENGINE=InnoDB;

-- Mapping Kanji ↔ Cụm (Mức độ×JLPT)
CREATE TABLE kanji_muc_do (
  ma_chu_kanji_muc_do BIGINT AUTO_INCREMENT PRIMARY KEY,
  ma_chu_kanji        BIGINT NOT NULL,
  muc_do_cap_do_id    INT    NOT NULL,
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_kanji_cum (ma_chu_kanji, muc_do_cap_do_id),
  CONSTRAINT fk_kmd_kanji FOREIGN KEY (ma_chu_kanji)     REFERENCES kanji(ma_chu_kanji)     ON DELETE CASCADE,
  CONSTRAINT fk_kmd_cum   FOREIGN KEY (muc_do_cap_do_id) REFERENCES muc_do_cap_do(id)       ON DELETE CASCADE
) ENGINE=InnoDB;

/* -----------------------------------------------------------------------------
   2) Seed data (roles, JLPT levels, one sample "Cơ Bản" + example Kanji)
----------------------------------------------------------------------------- */

INSERT INTO roles(role_id, role_name, description) VALUES
(1,'ADMIN','Quản trị'),
(2,'FREE','Người dùng miễn phí'),
(3,'VIP','Người dùng VIP');

INSERT INTO cap_do_jlpt(cap_do_code, thu_tu) VALUES
('N5',1),('N4',2),('N3',3),('N2',4),('N1',5);

-- Add one base Mức độ and generate clusters
INSERT INTO muc_do(ten_muc_do, mo_ta) VALUES ('Cơ Bản','Khóa nền tảng');

-- Cơ Bản × N5 = FREE (bật), others VIP (bật)
INSERT INTO muc_do_cap_do(ma_muc_do, cap_do_code, access_tier, is_enabled)
SELECT md.ma_muc_do, c.cap_do_code,
       CASE c.cap_do_code WHEN 'N5' THEN 'FREE' ELSE 'VIP' END,
       TRUE
FROM muc_do md
JOIN cap_do_jlpt c
WHERE md.ten_muc_do='Cơ Bản';

-- Example Kanji and attachments
INSERT INTO kanji(kanji, han_viet, am_on, am_kun, mo_ta) VALUES
('日','NHẬT','ニチ, ジツ','ひ, -び, -か','Mặt trời; ngày'),
('月','NGUYỆT','ゲツ, ガツ','つき','Mặt trăng; tháng');

INSERT INTO kanji_muc_do(ma_chu_kanji, muc_do_cap_do_id)
SELECT k.ma_chu_kanji, mdc.id
FROM kanji k
JOIN muc_do md        ON md.ten_muc_do='Cơ Bản'
JOIN muc_do_cap_do mdc ON mdc.ma_muc_do=md.ma_muc_do AND mdc.cap_do_code='N5'
WHERE k.kanji IN ('日','月');

/* -----------------------------------------------------------------------------
   3) View for convenient reading
----------------------------------------------------------------------------- */
CREATE OR REPLACE VIEW v_kanji_catalog AS
SELECT
  kmd.ma_chu_kanji_muc_do,
  k.ma_chu_kanji, k.kanji, k.han_viet, k.am_on, k.am_kun, k.mo_ta,
  mdc.id AS cum_id,
  md.ma_muc_do, md.ten_muc_do,
  c.cap_do_code,
  mdc.access_tier,
  mdc.is_enabled
FROM kanji_muc_do kmd
JOIN kanji k            ON kmd.ma_chu_kanji = k.ma_chu_kanji
JOIN muc_do_cap_do mdc  ON kmd.muc_do_cap_do_id = mdc.id
JOIN muc_do md          ON mdc.ma_muc_do = md.ma_muc_do
JOIN cap_do_jlpt c      ON mdc.cap_do_code = c.cap_do_code;

/* -----------------------------------------------------------------------------
   4) Stored Procedure: Upsert Mức độ × Cấp độ (FREE/VIP + enable/disable)
      (Parameters use VARCHAR/BOOLEAN for maximum compatibility)
----------------------------------------------------------------------------- */
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_upsert_muc_do_cap_do $$
CREATE PROCEDURE sp_upsert_muc_do_cap_do (
  IN  p_ten_muc_do   VARCHAR(100),
  IN  p_cap_do_code  CHAR(2),      -- 'N5'..'N1'
  IN  p_access_tier  VARCHAR(4),   -- 'FREE' or 'VIP'
  IN  p_is_enabled   BOOLEAN
)
BEGIN
  DECLARE v_ma_muc_do INT;

  -- Validate access tier
  IF (p_access_tier NOT IN ('FREE','VIP')) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'p_access_tier must be FREE or VIP';
  END IF;

  -- Ensure JLPT code exists
  IF (SELECT COUNT(*) FROM cap_do_jlpt WHERE cap_do_code = p_cap_do_code) = 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'cap_do_code invalid (N5..N1)';
  END IF;

  -- Upsert Mức độ
  SELECT ma_muc_do INTO v_ma_muc_do
  FROM muc_do
  WHERE ten_muc_do = p_ten_muc_do
  LIMIT 1;

  IF v_ma_muc_do IS NULL THEN
    INSERT INTO muc_do(ten_muc_do, mo_ta, is_active) VALUES (p_ten_muc_do, NULL, TRUE);
    SET v_ma_muc_do = LAST_INSERT_ID();
  END IF;

  -- Upsert cluster
  INSERT INTO muc_do_cap_do(ma_muc_do, cap_do_code, access_tier, is_enabled)
  VALUES (v_ma_muc_do, p_cap_do_code, p_access_tier, p_is_enabled)
  ON DUPLICATE KEY UPDATE
    access_tier = VALUES(access_tier),
    is_enabled  = VALUES(is_enabled);

  -- Return the cluster row
  SELECT id, ma_muc_do, cap_do_code, access_tier, is_enabled
  FROM muc_do_cap_do
  WHERE ma_muc_do = v_ma_muc_do AND cap_do_code = p_cap_do_code;
END $$

DELIMITER ;

/* -----------------------------------------------------------------------------
   5) Quick sanity checks (optional)
----------------------------------------------------------------------------- */
-- SELECT * FROM roles;
-- SELECT * FROM cap_do_jlpt ORDER BY thu_tu;
-- SELECT * FROM muc_do;
-- SELECT * FROM muc_do_cap_do ORDER BY ma_muc_do, cap_do_code;
-- SELECT * FROM v_kanji_catalog ORDER BY ten_muc_do, cap_do_code, kanji;

/* -----------------------------------------------------------------------------
   ALT: If you CANNOT drop database (e.g., limited privilege), you can instead:
   (Comment out the DROP DATABASE section above, then run the following first.)
----------------------------------------------------------------------------- */
-- SET FOREIGN_KEY_CHECKS=0;
-- DROP VIEW IF EXISTS v_kanji_catalog;
-- DROP PROCEDURE IF EXISTS sp_upsert_muc_do_cap_do;
-- DROP TABLE IF EXISTS kanji_muc_do, kanji, muc_do_cap_do, muc_do, cap_do_jlpt, users, roles;
-- SET FOREIGN_KEY_CHECKS=1;
-- -- Then re-run the CREATE statements above inside your existing database.
