-- Create Anggaran (Budget) table
-- Composite Primary Key: (tahun_anggaran, jenis_pajak)

CREATE TABLE IF NOT EXISTS system.anggaran (
    tahun_anggaran INTEGER NOT NULL,
    jenis_pajak VARCHAR(50) NOT NULL,
    nilai_anggaran DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (tahun_anggaran, jenis_pajak)
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_anggaran_tahun ON system.anggaran(tahun_anggaran);
CREATE INDEX IF NOT EXISTS idx_anggaran_jenis ON system.anggaran(jenis_pajak);

-- Add comment to table
COMMENT ON TABLE system.anggaran IS 'Tabel Anggaran Pajak per Tahun';
COMMENT ON COLUMN system.anggaran.tahun_anggaran IS 'Tahun anggaran (misal: 2024)';
COMMENT ON COLUMN system.anggaran.jenis_pajak IS 'Jenis pajak (PBB, BPHTB, dll)';
COMMENT ON COLUMN system.anggaran.nilai_anggaran IS 'Nilai anggaran dalam Rupiah';
