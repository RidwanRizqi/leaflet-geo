-- ===============================================
-- PBJT Potensi & Master Menu Schema
-- Database: pbjt_assessment_db (PostgreSQL)
-- ===============================================

CREATE TABLE IF NOT EXISTS pbjt_master_menus (
    id BIGSERIAL PRIMARY KEY,
    nama_menu VARCHAR(255) NOT NULL UNIQUE,
    kategori VARCHAR(50) NOT NULL DEFAULT 'FOOD',
    default_harga NUMERIC(15, 2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pbjt_potensi (
    id BIGSERIAL PRIMARY KEY,
    kode_potensi VARCHAR(50) NOT NULL UNIQUE,
    nama_usaha VARCHAR(255) NOT NULL,
    tipe_usaha VARCHAR(50),
    tanggal_pendataan DATE NOT NULL,
    kapasitas_kursi INTEGER DEFAULT 0,
    luas_bangunan NUMERIC(10, 2),
    jam_buka TIME,
    jam_tutup TIME,
    payment_methods TEXT[],
    alamat TEXT,
    kelurahan VARCHAR(100),
    kecamatan VARCHAR(100),
    kabupaten VARCHAR(100) DEFAULT 'Lumajang',
    kd_kec VARCHAR(10),
    kd_kel VARCHAR(10),
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    road_type VARCHAR(50),
    near_school BOOLEAN DEFAULT FALSE,
    near_office BOOLEAN DEFAULT FALSE,
    near_market BOOLEAN DEFAULT FALSE,
    crowd_level VARCHAR(20) DEFAULT 'NORMAL',
    opening_days_per_month INTEGER DEFAULT 30,
    menu_items JSONB,
    customer_order_estimates VARCHAR(1000),
    best_selling_menus VARCHAR(1000),
    crowd_peak_times VARCHAR(1000),
    omzet_bulanan NUMERIC(15, 2) DEFAULT 0,
    pbjt_bulanan NUMERIC(15, 2) DEFAULT 0,
    pbjt_tahunan NUMERIC(15, 2) DEFAULT 0,
    surveyor_id VARCHAR(50),
    updated_by VARCHAR(50),
    photo_urls TEXT[],
    supporting_doc_url TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_potensi_kode ON pbjt_potensi(kode_potensi);
CREATE INDEX IF NOT EXISTS idx_potensi_tanggal ON pbjt_potensi(tanggal_pendataan);
CREATE INDEX IF NOT EXISTS idx_potensi_kecamatan ON pbjt_potensi(kecamatan);
CREATE INDEX IF NOT EXISTS idx_potensi_kelurahan ON pbjt_potensi(kelurahan);
CREATE INDEX IF NOT EXISTS idx_master_menu_nama ON pbjt_master_menus(nama_menu);
