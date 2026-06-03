-- Seed data for Anggaran (Budget) table
-- Based on target hardcodes for year 2026

INSERT INTO system.anggaran (tahun_anggaran, jenis_pajak, nilai_anggaran) VALUES
(2026, 'Pajak Reklame', 2350000000),
(2026, 'Pajak Air Tanah', 1200000000),
(2026, 'Pajak Mineral Bukan Logam dan Batuan', 29000000000),
(2026, 'Pajak Bumi dan Bangunan Perdesaan dan Perkotaan (PBBP2)', 24000000000),
(2026, 'Bea Perolehan Hak Atas Tanah dan Bangunan (BPHTB)', 22100000000),
(2026, 'PBJT-Makanan dan/atau Minuman', 5150000000),
(2026, 'PBJT-Tenaga Listrik', 45000000000),
(2026, 'PBJT-Jasa Perhotelan', 2000000000),
(2026, 'PBJT-Jasa Parkir', 550000000),
(2026, 'PBJT-Jasa Kesenian dan Hiburan', 1000000000),
(2026, 'Opsen PKB', 46607465200),
(2026, 'Opsen BBNKB', 13962242300)
ON CONFLICT (tahun_anggaran, jenis_pajak) 
DO UPDATE SET 
    nilai_anggaran = EXCLUDED.nilai_anggaran,
    updated_at = NOW();
