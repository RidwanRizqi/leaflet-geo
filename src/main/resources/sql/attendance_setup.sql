-- ========================================================
-- DDL & SEED DATA UNTUK FITUR ABSENSI (SIG RIDWAN)
-- Schema: system
-- Database: PostgreSQL (sig)
-- ========================================================

-- 1. TABEL OFFICE LOCATION (Titik Acuan Kantor)
CREATE TABLE IF NOT EXISTS system.office_location (
    id              SERIAL PRIMARY KEY,
    nama            VARCHAR(100) NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    radius_meter    INTEGER NOT NULL DEFAULT 150,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- Insert / Update Default Lokasi Kantor BPRD Lumajang
INSERT INTO system.office_location (nama, latitude, longitude, radius_meter, is_active)
SELECT 'Kantor BPRD Lumajang', -8.134386886649747, 113.22632802023689, 150, true
WHERE NOT EXISTS (SELECT 1 FROM system.office_location WHERE nama = 'Kantor BPRD Lumajang');

UPDATE system.office_location
SET latitude = -8.134291290470719, longitude = 113.22636556686881
WHERE nama = 'Kantor BPRD Lumajang';


-- 2. TABEL ATTENDANCE SETTING (Konfigurasi per Role)
CREATE TABLE IF NOT EXISTS system.attendance_setting (
    id                  SERIAL PRIMARY KEY,
    role_name           VARCHAR(25) NOT NULL UNIQUE,
    is_mandatory        BOOLEAN NOT NULL DEFAULT FALSE,
    default_landing_url VARCHAR(100) DEFAULT '/dashboard-pajak',
    jam_masuk_start     TIME DEFAULT '07:00:00',
    jam_masuk_end       TIME DEFAULT '09:00:00',
    jam_pulang_start    TIME DEFAULT '1:00:00',
    jam_pulang_end      TIME DEFAULT '18:00:00',
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);

-- Seed Settings untuk Role
INSERT INTO system.attendance_setting (role_name, is_mandatory, default_landing_url)
VALUES 
    ('MAGANG',        TRUE,  '/absensi'),
    ('STAFF',         FALSE,  '/dashboard-pajak'),
    ('KEPALA_BIDANG', FALSE,  '/dashboard-pajak'),
    ('ADMIN',         FALSE, '/dashboard-pajak'),
    ('VIEWER',        FALSE, '/dashboard-pajak')
ON CONFLICT (role_name) DO UPDATE 
SET is_mandatory = EXCLUDED.is_mandatory,
    default_landing_url = EXCLUDED.default_landing_url,
    updated_at = NOW();


-- 3. TABEL ATTENDANCE (Data Absensi Harian)
CREATE TABLE IF NOT EXISTS system.attendance (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES system."user"(id) ON DELETE CASCADE,
    
    -- Waktu
    tanggal         DATE NOT NULL DEFAULT CURRENT_DATE,
    jam_masuk       TIMESTAMP,
    jam_pulang      TIMESTAMP,
    
    -- Lokasi Masuk
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    lokasi_status   VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN', -- 'DI_KANTOR' | 'DI_LUAR_KANTOR'
    jarak_meter     DOUBLE PRECISION,
    gps_accuracy    DOUBLE PRECISION,
    
    -- Lokasi Pulang
    pulang_latitude      DOUBLE PRECISION,
    pulang_longitude     DOUBLE PRECISION,
    pulang_lokasi_status VARCHAR(20),
    pulang_jarak_meter   DOUBLE PRECISION,
    pulang_gps_accuracy  DOUBLE PRECISION,
    
    -- Foto Selfie
    foto_masuk      TEXT,
    foto_pulang     TEXT,
    
    -- Metadata Device & Audit
    device_info     VARCHAR(255),
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    
    -- 1 user hanya 1 absensi per tanggal
    CONSTRAINT uq_attendance_user_tanggal UNIQUE (user_id, tanggal)
);

CREATE INDEX IF NOT EXISTS idx_attendance_user_id ON system.attendance(user_id);
CREATE INDEX IF NOT EXISTS idx_attendance_tanggal ON system.attendance(tanggal);
CREATE INDEX IF NOT EXISTS idx_attendance_lokasi ON system.attendance(lokasi_status);


-- 4. SEED SAMPLE USER UNTUK ROLE MAGANG (JIKA BELUM ADA)
-- Password: 'admin' (BCrypt hash $2a$10$W6FTOwaJq8jqK4LKLGqga.dJN2Go3FKboNlthD7JvykeOE7r99Ewm)
INSERT INTO system.user (id, username, password, nama, role, is_admin, is_active, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'magang', '$2a$10$W6FTOwaJq8jqK4LKLGqga.dJN2Go3FKboNlthD7JvykeOE7r99Ewm', 'Peserta Magang 1', 'MAGANG', false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system.user WHERE username = 'magang');

-- 5. SEED ROLE MENU MAPPING UNTUK MAGANG & ADMIN (Menu ID 12 untuk Absensi)
-- Menu 12 = Menu Absensi
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'MAGANG', 12, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system.role_menu_mapping WHERE role_name = 'MAGANG' AND menu_id = 12);

INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'ADMIN', 12, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system.role_menu_mapping WHERE role_name = 'ADMIN' AND menu_id = 12);
