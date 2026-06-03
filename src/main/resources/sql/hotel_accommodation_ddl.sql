-- ============================================================
-- DDL: Tabel Hotel Accommodations & Hotel Realisasi
-- Database: pbjt_assessment_db (PostgreSQL)
-- Digunakan oleh: HotelAccommodationService.java
-- ============================================================

-- ============================================================
-- 1. TABEL hotel_accommodations
--    Menyimpan data properti hotel/akomodasi (PBJT Jasa Perhotelan)
--    Data bisa dari SIMATDA (sync) atau di-input manual.
-- ============================================================
CREATE TABLE IF NOT EXISTS hotel_accommodations (
    -- Primary Key
    id                      BIGSERIAL PRIMARY KEY,

    -- SIMATDA Reference (nullable — bisa null kalau entry manual)
    simatda_id              INTEGER UNIQUE,          -- t_idobjek dari SIMATDA
    simatda_wp_id           INTEGER,                 -- t_idwp dari SIMATDA
    object_number           VARCHAR(50),             -- t_nop / nomor objek pajak

    -- Tipe Akomodasi
    -- Nilai: HOTEL | WISMA | HOMESTAY | PENGINAPAN | RUMAH_KOS
    accommodation_type      VARCHAR(30) NOT NULL DEFAULT 'HOTEL',

    -- Identitas Properti
    property_name           VARCHAR(255) NOT NULL,
    owner_name              VARCHAR(255),
    owner_phone             VARCHAR(30),
    npwpd                   VARCHAR(50),             -- Nomor Pokok Wajib Pajak Daerah

    -- Lokasi
    address                 TEXT,
    kelurahan               VARCHAR(100),
    kecamatan               VARCHAR(100),
    kabupaten               VARCHAR(100) DEFAULT 'KABUPATEN LUMAJANG',
    latitude                DECIMAL(10, 7),
    longitude               DECIMAL(10, 7),

    -- Data Fisik
    total_rooms             INTEGER,
    building_area           DECIMAL(12, 2),          -- Luas bangunan (m²)
    land_area               DECIMAL(12, 2),          -- Luas tanah (m²)

    -- Status Legalitas
    has_business_permit     BOOLEAN DEFAULT FALSE,   -- Apakah punya izin usaha
    has_tax_registration    BOOLEAN DEFAULT FALSE,   -- Apakah sudah terdaftar pajak
    willing_to_formalize    BOOLEAN,                 -- Bersedia formalisasi (null = belum ditanya)

    -- Status Formalisasi
    -- Nilai: FORMAL | SEMI_FORMAL | INFORMAL
    formalization_status    VARCHAR(20) DEFAULT 'INFORMAL',

    -- Data Finansial
    estimated_annual_revenue    DECIMAL(20, 2),      -- Estimasi pendapatan tahunan (Rp)
    projected_annual_tax        DECIMAL(20, 2),      -- Proyeksi pajak tahunan (Rp)
    tax_rate                    DECIMAL(5, 4) DEFAULT 0.10, -- Tarif pajak (default 10%)
    tax_object_id               VARCHAR(50),         -- ID objek pajak internal

    -- Status Operasional
    -- Nilai: ACTIVE | CLOSED | INACTIVE | PENDING
    status                  VARCHAR(20) DEFAULT 'ACTIVE',
    is_closed               BOOLEAN DEFAULT FALSE,   -- Flag dari SIMATDA (t_objektutup)

    -- Foto & Dokumen
    photo_urls              TEXT[],                  -- Array URL foto properti (max 4)
    supporting_doc_url      TEXT,                    -- URL dokumen pendukung

    -- Audit Fields
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_at               TIMESTAMP               -- Kapan terakhir di-sync dari SIMATDA
);

-- Index untuk pencarian umum
CREATE INDEX IF NOT EXISTS idx_hotel_property_name ON hotel_accommodations (property_name);
CREATE INDEX IF NOT EXISTS idx_hotel_kecamatan ON hotel_accommodations (kecamatan);
CREATE INDEX IF NOT EXISTS idx_hotel_status ON hotel_accommodations (status);
CREATE INDEX IF NOT EXISTS idx_hotel_accommodation_type ON hotel_accommodations (accommodation_type);
CREATE INDEX IF NOT EXISTS idx_hotel_formalization_status ON hotel_accommodations (formalization_status);
CREATE INDEX IF NOT EXISTS idx_hotel_simatda_id ON hotel_accommodations (simatda_id);

-- Komentar tabel
COMMENT ON TABLE hotel_accommodations IS 'Data properti hotel/akomodasi untuk PBJT Jasa Perhotelan. Dapat di-sync dari SIMATDA atau diinput manual.';
COMMENT ON COLUMN hotel_accommodations.simatda_id IS 'ID objek dari SIMATDA (t_idobjek). NULL jika entry manual.';
COMMENT ON COLUMN hotel_accommodations.accommodation_type IS 'Tipe akomodasi: HOTEL, WISMA, HOMESTAY, PENGINAPAN, RUMAH_KOS';
COMMENT ON COLUMN hotel_accommodations.formalization_status IS 'Status formalisasi: FORMAL, SEMI_FORMAL, INFORMAL';
COMMENT ON COLUMN hotel_accommodations.photo_urls IS 'Array URL foto properti, maksimal 4 foto';


-- ============================================================
-- 2. TABEL hotel_realisasi
--    Menyimpan data realisasi pajak hotel per tahun.
--    Data di-sync dari tabel t_transaksi SIMATDA (read-only).
-- ============================================================
CREATE TABLE IF NOT EXISTS hotel_realisasi (
    -- Primary Key
    id                  BIGSERIAL PRIMARY KEY,

    -- Relasi ke hotel_accommodations
    hotel_id            BIGINT NOT NULL REFERENCES hotel_accommodations(id) ON DELETE CASCADE,

    -- SIMATDA Reference
    simatda_objek_id    INTEGER,                     -- t_idwpobjek dari SIMATDA t_transaksi

    -- Periode
    tahun               VARCHAR(4) NOT NULL,         -- Tahun pajak, misal '2024'

    -- Data Finansial (agregasi per tahun dari SIMATDA)
    total_revenue       BIGINT DEFAULT 0,            -- Total dasar pengenaan pajak (Rp)
    total_tax           BIGINT DEFAULT 0,            -- Total pajak yang ditetapkan (Rp)
    total_payment       BIGINT DEFAULT 0,            -- Total pembayaran diterima (Rp)
    transaction_count   INTEGER DEFAULT 0,           -- Jumlah transaksi dalam tahun ini

    -- Audit
    synced_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Unique constraint: satu record per hotel per tahun
    CONSTRAINT uq_hotel_realisasi UNIQUE (hotel_id, tahun)
);

-- Index
CREATE INDEX IF NOT EXISTS idx_hotel_realisasi_hotel_id ON hotel_realisasi (hotel_id);
CREATE INDEX IF NOT EXISTS idx_hotel_realisasi_tahun ON hotel_realisasi (tahun);
CREATE INDEX IF NOT EXISTS idx_hotel_realisasi_simatda ON hotel_realisasi (simatda_objek_id);

-- Komentar tabel
COMMENT ON TABLE hotel_realisasi IS 'Data realisasi pajak hotel per tahun. Di-sync dari t_transaksi SIMATDA (read-only).';
COMMENT ON COLUMN hotel_realisasi.tahun IS 'Tahun pajak, format 4 digit: 2022, 2023, 2024, dst.';
COMMENT ON COLUMN hotel_realisasi.total_revenue IS 'Total dasar pengenaan pajak (omzet) dalam Rupiah';
COMMENT ON COLUMN hotel_realisasi.total_tax IS 'Total pajak yang ditetapkan dalam Rupiah';
COMMENT ON COLUMN hotel_realisasi.total_payment IS 'Total pembayaran yang diterima dalam Rupiah';


-- ============================================================
-- 3. VERIFIKASI — Cek tabel berhasil dibuat
-- ============================================================
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public'
-- AND table_name IN ('hotel_accommodations', 'hotel_realisasi');
