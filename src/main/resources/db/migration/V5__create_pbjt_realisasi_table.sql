-- Table untuk menyimpan data realisasi PBJT per tahun
-- Data ini di-seed sekali dari SIMATDA, sehingga aplikasi tidak perlu connect ke SIMATDA lagi

CREATE TABLE IF NOT EXISTS pbjt_realisasi (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    tax_object_id VARCHAR(50),
    nop VARCHAR(50),
    business_name VARCHAR(255),
    tahun INTEGER NOT NULL,
    realisasi_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    jumlah_transaksi INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key ke pbjt_assessments
    CONSTRAINT fk_pbjt_realisasi_assessment 
        FOREIGN KEY (assessment_id) 
        REFERENCES pbjt_assessments(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint untuk mencegah duplikasi
    CONSTRAINT uk_realisasi_assessment_tahun 
        UNIQUE (assessment_id, tahun)
);

-- Index untuk performance
CREATE INDEX idx_pbjt_realisasi_assessment_id ON pbjt_realisasi(assessment_id);
CREATE INDEX idx_pbjt_realisasi_tahun ON pbjt_realisasi(tahun);
CREATE INDEX idx_pbjt_realisasi_tax_object_id ON pbjt_realisasi(tax_object_id);
CREATE INDEX idx_pbjt_realisasi_nop ON pbjt_realisasi(nop);

-- Comments
COMMENT ON TABLE pbjt_realisasi IS 'Data realisasi pajak PBJT per tahun dari SIMATDA (2021-2025)';
COMMENT ON COLUMN pbjt_realisasi.assessment_id IS 'ID assessment PBJT';
COMMENT ON COLUMN pbjt_realisasi.tax_object_id IS 'ID objek pajak dari SIMATDA';
COMMENT ON COLUMN pbjt_realisasi.nop IS 'Nomor Objek Pajak dari SIMATDA';
COMMENT ON COLUMN pbjt_realisasi.tahun IS 'Tahun realisasi (2021-2025)';
COMMENT ON COLUMN pbjt_realisasi.realisasi_amount IS 'Total realisasi dalam rupiah';
COMMENT ON COLUMN pbjt_realisasi.jumlah_transaksi IS 'Jumlah transaksi dalam tahun tersebut';
