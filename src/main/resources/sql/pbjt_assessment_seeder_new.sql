-- ===============================================
-- PBJT Assessment Sample Data Seeder
-- Updated to match Master Data from Bidang API
-- Database: pbjt_assessment_db
-- Lumajang Regency (kdProp: 35, kdDati2: 08)
-- ===============================================

-- Clear existing sample data (optional - uncomment if needed)
-- DELETE FROM pbjt_observation_history;
-- DELETE FROM pbjt_assessments WHERE business_id LIKE 'BIZ-%';

-- ===============================================
-- SAMPLE ASSESSMENT DATA WITH REAL LOCATIONS
-- ===============================================

-- Kecamatan: LUMAJANG (kdKec: 060)
-- Kelurahan: JOGOYUDAN, KEPUHARJO, TOMPOKERSAN, ROGOTRUNAN, etc.
INSERT INTO pbjt_assessments (
    business_id, business_name, assessment_date,
    building_area, seating_capacity, 
    operating_hours_start, operating_hours_end,
    business_type, payment_methods,
    daily_revenue_weekday, daily_revenue_weekend,
    monthly_revenue_raw, monthly_revenue_adjusted,
    business_type_coefficient, location_score, operational_rate,
    monthly_pbjt, annual_pbjt,
    tax_rate, inflation_rate,
    confidence_score, confidence_level,
    surveyor_id, taxpayer_signed,
    latitude, longitude, address, kelurahan, kecamatan, kabupaten,
    photo_urls
) VALUES
(
    'BIZ-001', 'Warung Makan Sederhana', '2026-01-05',
    75.50, 25,
    '08:00:00', '20:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    450000.00, 620000.00,
    12100000.00, 11700000.00,
    0.85, 1.15, 0.90,
    1170000.00, 14414400.00,
    0.10, 0.03,
    72, 'MEDIUM',
    'SURV-001', TRUE,
    -8.125000, 113.718750, 'Jl. Pahlawan No. 45', 'TOMPOKERSAN', 'LUMAJANG', 'Lumajang',
    ARRAY['photo1.jpg', 'photo2.jpg']
),
(
    'BIZ-002', 'Restaurant Nikmat Rasa', '2026-01-04',
    180.00, 75,
    '10:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1850000.00, 2650000.00,
    49300000.00, 62500000.00,
    1.10, 1.25, 0.90,
    6250000.00, 77025000.00,
    0.10, 0.03,
    85, 'HIGH',
    'SURV-001', TRUE,
    -8.133333, 113.716667, 'Jl. Basuki Rahmat No. 123', 'ROGOTRUNAN', 'LUMAJANG', 'Lumajang',
    ARRAY['photo3.jpg', 'photo4.jpg', 'photo5.jpg']
),

-- Kecamatan: CANDIPURO (kdKec: 030)
-- Kelurahan: YUGOSARI, CANDIPURO, SUMBER REJO, etc.
(
    'BIZ-003', 'Café Modern Hits', '2026-01-03',
    120.00, 50,
    '09:00:00', '23:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    1250000.00, 1880000.00,
    33250000.00, 47880000.00,
    1.20, 1.22, 0.90,
    4788000.00, 59004480.00,
    0.10, 0.03,
    78, 'MEDIUM',
    'SURV-002', TRUE,
    -8.186500, 113.083200, 'Jl. Semeru No. 88', 'YUGOSARI', 'CANDIPURO', 'Lumajang',
    ARRAY['photo6.jpg', 'photo7.jpg']
),
(
    'BIZ-004', 'Warung Kopi Tradisional', '2026-01-06',
    45.00, 15,
    '06:00:00', '18:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    280000.00, 350000.00,
    7800000.00, 7500000.00,
    0.80, 1.05, 0.85,
    750000.00, 9247500.00,
    0.10, 0.03,
    65, 'MEDIUM',
    'SURV-003', TRUE,
    -8.182300, 113.079500, 'Jl. Raya Candipuro No. 12', 'SUMBER REJO', 'CANDIPURO', 'Lumajang',
    ARRAY['photo8.jpg']
),

-- Kecamatan: JATIROTO (kdKec: 100)
(
    'BIZ-005', 'Depot Nasi Pecel Bu Sri', '2026-01-07',
    90.00, 40,
    '07:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    680000.00, 920000.00,
    18200000.00, 17500000.00,
    0.90, 1.10, 0.88,
    1750000.00, 21577500.00,
    0.10, 0.03,
    74, 'MEDIUM',
    'SURV-001', TRUE,
    -8.226700, 113.244400, 'Jl. Mastrip No. 56', 'JATIROTO', 'JATIROTO', 'Lumajang',
    ARRAY['photo9.jpg', 'photo10.jpg']
),

-- Kecamatan: PASIRIAN (kdKec: 040)
(
    'BIZ-006', 'Resto Seafood Bahari', '2026-01-08',
    200.00, 80,
    '11:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Transfer'],
    2100000.00, 3200000.00,
    58800000.00, 71500000.00,
    1.15, 1.28, 0.92,
    7150000.00, 88159500.00,
    0.10, 0.03,
    88, 'HIGH',
    'SURV-002', TRUE,
    -8.093200, 113.511600, 'Jl. Pantai Pasirian No. 7', 'PASIRIAN', 'PASIRIAN', 'Lumajang',
    ARRAY['photo11.jpg', 'photo12.jpg', 'photo13.jpg']
),

-- Kecamatan: PRONOJIWO (kdKec: 020)
(
    'BIZ-007', 'Warung Tegal Pak Hadi', '2026-01-09',
    60.00, 20,
    '05:00:00', '15:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    320000.00, 380000.00,
    8560000.00, 8200000.00,
    0.82, 1.08, 0.86,
    820000.00, 10110600.00,
    0.10, 0.03,
    68, 'MEDIUM',
    'SURV-003', TRUE,
    -8.048900, 113.651200, 'Jl. Raya Pronojiwo No. 34', 'PRONOJIWO', 'PRONOJIWO', 'Lumajang',
    ARRAY['photo14.jpg']
),

-- Kecamatan: TEMPEH (kdKec: 050)
(
    'BIZ-008', 'Café & Bakery Sweet Corner', '2026-01-10',
    140.00, 55,
    '08:00:00', '22:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    1450000.00, 2150000.00,
    38650000.00, 53500000.00,
    1.18, 1.20, 0.89,
    5350000.00, 65940500.00,
    0.10, 0.03,
    80, 'HIGH',
    'SURV-001', TRUE,
    -8.052800, 113.467900, 'Jl. Ahmad Yani Tempeh No. 99', 'TEMPEH', 'T E M P E H', 'Lumajang',
    ARRAY['photo15.jpg', 'photo16.jpg']
),

-- Kecamatan: SUKODONO (kdKec: 120)
(
    'BIZ-009', 'Rumah Makan Padang Sederhana', '2026-01-11',
    100.00, 45,
    '08:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    850000.00, 1150000.00,
    22950000.00, 22200000.00,
    0.92, 1.12, 0.87,
    2220000.00, 27368400.00,
    0.10, 0.03,
    76, 'MEDIUM',
    'SURV-002', TRUE,
    -8.148900, 113.906700, 'Jl. Raya Sukodono No. 45', 'SUKODONO', 'SUKODONO', 'Lumajang',
    ARRAY['photo17.jpg', 'photo18.jpg']
),

-- Kecamatan: TEMPUR SARI (kdKec: 010)
(
    'BIZ-010', 'Depot Soto Lamongan Bu Ning', '2026-01-12',
    70.00, 30,
    '07:00:00', '19:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    550000.00, 720000.00,
    14850000.00, 14200000.00,
    0.88, 1.10, 0.88,
    1420000.00, 17504400.00,
    0.10, 0.03,
    71, 'MEDIUM',
    'SURV-003', TRUE,
    -8.002100, 113.732800, 'Jl. Raya Tempursari No. 78', 'TEMPURSARI', 'TEMPUR SARI', 'Lumajang',
    ARRAY['photo19.jpg']
),

-- Kecamatan: YOSOWILANGUN (kdKec: 090)
(
    'BIZ-011', 'Restaurant & Catering Berkah', '2026-01-13',
    220.00, 90,
    '10:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Transfer'],
    2400000.00, 3600000.00,
    67200000.00, 82500000.00,
    1.12, 1.26, 0.91,
    8250000.00, 101722500.00,
    0.10, 0.03,
    86, 'HIGH',
    'SURV-001', TRUE,
    -8.261700, 113.702800, 'Jl. Diponegoro No. 156', 'YOSOWILANGUN', 'YOSOWILANGUN', 'Lumajang',
    ARRAY['photo20.jpg', 'photo21.jpg', 'photo22.jpg']
),

-- Kecamatan: SENDURO (kdKec: 130)
(
    'BIZ-012', 'Warung Kopi & Snack Sunrise', '2026-01-14',
    55.00, 18,
    '06:00:00', '17:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    310000.00, 420000.00,
    8370000.00, 8000000.00,
    0.81, 1.06, 0.85,
    800000.00, 9864000.00,
    0.10, 0.03,
    66, 'MEDIUM',
    'SURV-002', TRUE,
    -8.244400, 113.046700, 'Jl. Raya Senduro No. 23', 'SENDURO', 'SENDURO', 'Lumajang',
    ARRAY['photo23.jpg']
),

-- Kecamatan: K U N I R (kdKec: 080)
(
    'BIZ-013', 'Bakso & Mie Ayam Pak Eko', '2026-01-15',
    65.00, 28,
    '09:00:00', '21:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    480000.00, 650000.00,
    12840000.00, 12400000.00,
    0.86, 1.09, 0.87,
    1240000.00, 15288800.00,
    0.10, 0.03,
    73, 'MEDIUM',
    'SURV-003', TRUE,
    -8.269400, 113.467200, 'Jl. Raya Kunir No. 67', 'KUNIR', 'K U N I R', 'Lumajang',
    ARRAY['photo24.jpg', 'photo25.jpg']
),

-- Additional assessments for LUMAJANG (kdKec: 060)
(
    'BIZ-014', 'Warung Soto Ayam Bu Tini', '2026-01-16',
    55.00, 20,
    '06:00:00', '16:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    340000.00, 450000.00,
    9180000.00, 8800000.00,
    0.83, 1.12, 0.86,
    880000.00, 10847200.00,
    0.10, 0.03,
    69, 'MEDIUM',
    'SURV-001', TRUE,
    -8.131500, 113.720100, 'Jl. Basuki Rahmat No. 234', 'JOGOYUDAN', 'LUMAJANG', 'Lumajang',
    ARRAY['photo26.jpg']
),
(
    'BIZ-015', 'Resto Lesehan Ayam Bakar', '2026-01-16',
    150.00, 60,
    '11:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1650000.00, 2400000.00,
    44550000.00, 58000000.00,
    1.08, 1.20, 0.91,
    5800000.00, 71494000.00,
    0.10, 0.03,
    82, 'HIGH',
    'SURV-002', TRUE,
    -8.127800, 113.716200, 'Jl. Slamet Riyadi No. 45', 'DITOTRUNAN', 'LUMAJANG', 'Lumajang',
    ARRAY['photo27.jpg', 'photo28.jpg']
),
(
    'BIZ-016', 'Kedai Kopi & Roti Pagi', '2026-01-17',
    42.00, 16,
    '05:30:00', '14:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    290000.00, 380000.00,
    7830000.00, 7400000.00,
    0.81, 1.08, 0.84,
    740000.00, 9124400.00,
    0.10, 0.03,
    67, 'MEDIUM',
    'SURV-003', TRUE,
    -8.135200, 113.721500, 'Jl. Veteran No. 12', 'LABRUK LOR', 'LUMAJANG', 'Lumajang',
    ARRAY['photo29.jpg']
),

-- More for CANDIPURO (kdKec: 030)
(
    'BIZ-017', 'Warung Pecel Lele Mbak Ida', '2026-01-17',
    68.00, 30,
    '09:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    580000.00, 780000.00,
    15660000.00, 15000000.00,
    0.89, 1.11, 0.87,
    1500000.00, 18495000.00,
    0.10, 0.03,
    72, 'MEDIUM',
    'SURV-001', TRUE,
    -8.184200, 113.081500, 'Jl. Raya Semeru No. 145', 'J A R I T', 'CANDIPURO', 'Lumajang',
    ARRAY['photo30.jpg', 'photo31.jpg']
),
(
    'BIZ-018', 'Resto Ikan Bakar Samudra', '2026-01-18',
    175.00, 70,
    '10:00:00', '21:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1750000.00, 2550000.00,
    47250000.00, 62000000.00,
    1.12, 1.24, 0.90,
    6200000.00, 76426000.00,
    0.10, 0.03,
    84, 'HIGH',
    'SURV-002', TRUE,
    -8.188900, 113.077800, 'Jl. Gunung Semeru No. 88', 'SUMBERWULUH', 'CANDIPURO', 'Lumajang',
    ARRAY['photo32.jpg', 'photo33.jpg', 'photo34.jpg']
),
(
    'BIZ-019', 'Café Gunung View', '2026-01-18',
    95.00, 40,
    '08:00:00', '20:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    950000.00, 1350000.00,
    25650000.00, 35000000.00,
    1.15, 1.18, 0.88,
    3500000.00, 43155000.00,
    0.10, 0.03,
    76, 'MEDIUM',
    'SURV-003', TRUE,
    -8.191200, 113.085400, 'Jl. Wisata Bromo No. 22', 'PENANGGAL', 'CANDIPURO', 'Lumajang',
    ARRAY['photo35.jpg', 'photo36.jpg']
),

-- JATIROTO (kdKec: 100)
(
    'BIZ-020', 'Warung Nasi Goreng 24 Jam', '2026-01-19',
    48.00, 18,
    '00:00:00', '23:59:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    420000.00, 580000.00,
    11340000.00, 10900000.00,
    0.84, 1.10, 0.86,
    1090000.00, 13434700.00,
    0.10, 0.03,
    70, 'MEDIUM',
    'SURV-001', TRUE,
    -8.228900, 113.246700, 'Jl. Pabrik No. 23', 'JATIROTO', 'JATIROTO', 'Lumajang',
    ARRAY['photo37.jpg']
),

-- PASIRIAN (kdKec: 040)
(
    'BIZ-021', 'Depot Ikan Segar Laut Selatan', '2026-01-19',
    135.00, 55,
    '10:00:00', '21:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1550000.00, 2300000.00,
    41850000.00, 55000000.00,
    1.10, 1.22, 0.89,
    5500000.00, 67782500.00,
    0.10, 0.03,
    81, 'HIGH',
    'SURV-002', TRUE,
    -8.094500, 113.513200, 'Jl. Pantai Selatan No. 15', 'PASIRIAN', 'PASIRIAN', 'Lumajang',
    ARRAY['photo38.jpg', 'photo39.jpg']
),

-- PRONOJIWO (kdKec: 020)
(
    'BIZ-022', 'Rumah Makan Sunda Cita Rasa', '2026-01-20',
    88.00, 38,
    '08:00:00', '20:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    720000.00, 980000.00,
    19440000.00, 18800000.00,
    0.91, 1.11, 0.88,
    1880000.00, 23182800.00,
    0.10, 0.03,
    75, 'MEDIUM',
    'SURV-003', TRUE,
    -8.051200, 113.653400, 'Jl. Raya Pronojiwo No. 67', 'PRONOJIWO', 'PRONOJIWO', 'Lumajang',
    ARRAY['photo40.jpg', 'photo41.jpg']
),

-- T E M P E H (kdKec: 050)
(
    'BIZ-023', 'Warung Nasi Kuning Bu Wati', '2026-01-20',
    52.00, 22,
    '05:00:00', '13:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    360000.00, 480000.00,
    9720000.00, 9300000.00,
    0.82, 1.09, 0.85,
    930000.00, 11464500.00,
    0.10, 0.03,
    68, 'MEDIUM',
    'SURV-001', TRUE,
    -8.054500, 113.470100, 'Jl. Pasar Tempeh No. 8', 'TEMPEH', 'T E M P E H', 'Lumajang',
    ARRAY['photo42.jpg']
),
(
    'BIZ-024', 'Restaurant Lesehan Jogja', '2026-01-21',
    165.00, 68,
    '10:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1680000.00, 2450000.00,
    45360000.00, 59500000.00,
    1.11, 1.23, 0.90,
    5950000.00, 73339250.00,
    0.10, 0.03,
    83, 'HIGH',
    'SURV-002', TRUE,
    -8.056800, 113.472300, 'Jl. Raya Tempeh No. 122', 'TEMPEH', 'T E M P E H', 'Lumajang',
    ARRAY['photo43.jpg', 'photo44.jpg']
),

-- SUKODONO (kdKec: 120)
(
    'BIZ-025', 'Bakso President Pak Bambang', '2026-01-21',
    72.00, 32,
    '09:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    620000.00, 850000.00,
    16740000.00, 16200000.00,
    0.90, 1.10, 0.87,
    1620000.00, 19978200.00,
    0.10, 0.03,
    74, 'MEDIUM',
    'SURV-003', TRUE,
    -8.150200, 113.908900, 'Jl. Raya Sukodono No. 78', 'SUKODONO', 'SUKODONO', 'Lumajang',
    ARRAY['photo45.jpg', 'photo46.jpg']
),

-- YOSOWILANGUN (kdKec: 090)
(
    'BIZ-026', 'Depot Rawon Bu Kanjeng', '2026-01-22',
    78.00, 35,
    '07:00:00', '19:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    660000.00, 890000.00,
    17820000.00, 17200000.00,
    0.91, 1.11, 0.88,
    1720000.00, 21209200.00,
    0.10, 0.03,
    75, 'MEDIUM',
    'SURV-001', TRUE,
    -8.263900, 113.705100, 'Jl. Diponegoro No. 234', 'YOSOWILANGUN', 'YOSOWILANGUN', 'Lumajang',
    ARRAY['photo47.jpg']
),

-- SENDURO (kdKec: 130)
(
    'BIZ-027', 'Warung Wedang Ronde Pak Tarno', '2026-01-22',
    38.00, 14,
    '15:00:00', '23:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    240000.00, 320000.00,
    6480000.00, 6200000.00,
    0.80, 1.06, 0.84,
    620000.00, 7643600.00,
    0.10, 0.03,
    64, 'MEDIUM',
    'SURV-002', TRUE,
    -8.246700, 113.048900, 'Jl. Senduro Barat No. 45', 'SENDURO', 'SENDURO', 'Lumajang',
    ARRAY['photo48.jpg']
),

-- TEMPUR SARI (kdKec: 010)
(
    'BIZ-028', 'Warung Sate Kambing Mas Joko', '2026-01-23',
    62.00, 26,
    '10:00:00', '22:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    580000.00, 800000.00,
    15660000.00, 15100000.00,
    0.89, 1.10, 0.87,
    1510000.00, 18607300.00,
    0.10, 0.03,
    73, 'MEDIUM',
    'SURV-003', TRUE,
    -8.004300, 113.735100, 'Jl. Raya Tempursari No. 156', 'TEMPURSARI', 'TEMPUR SARI', 'Lumajang',
    ARRAY['photo49.jpg', 'photo50.jpg']
),

-- SUMBER SUKO (kdKec: 061)
(
    'BIZ-029', 'Café Kopi Sumbersuko', '2026-01-23',
    85.00, 36,
    '07:00:00', '21:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    820000.00, 1180000.00,
    22140000.00, 30500000.00,
    1.14, 1.17, 0.87,
    3050000.00, 37597750.00,
    0.10, 0.03,
    77, 'MEDIUM',
    'SURV-001', TRUE,
    -8.112300, 113.745600, 'Jl. Sumbersuko No. 34', 'SUMBERSUKO', 'SUMBER SUKO', 'Lumajang',
    ARRAY['photo51.jpg', 'photo52.jpg']
),

-- TEKUNG (kdKec: 070)
(
    'BIZ-030', 'Warung Pecel Ayam Mbok Jum', '2026-01-24',
    58.00, 24,
    '08:00:00', '20:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    420000.00, 560000.00,
    11340000.00, 10900000.00,
    0.84, 1.09, 0.86,
    1090000.00, 13434700.00,
    0.10, 0.03,
    70, 'MEDIUM',
    'SURV-002', TRUE,
    -8.195600, 113.612300, 'Jl. Tekung Raya No. 23', 'TEKUNG', 'T E K U N G', 'Lumajang',
    ARRAY['photo53.jpg']
),

-- ROWOKANGKUNG (kdKec: 101)
(
    'BIZ-031', 'Resto Ayam Geprek Viral', '2026-01-24',
    92.00, 42,
    '10:00:00', '22:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS', 'EDC'],
    950000.00, 1300000.00,
    25650000.00, 25000000.00,
    0.95, 1.13, 0.88,
    2500000.00, 30825000.00,
    0.10, 0.03,
    78, 'MEDIUM',
    'SURV-003', TRUE,
    -8.243500, 113.312800, 'Jl. Rowokangkung No. 67', 'ROWOKANGKUNG', 'ROWOKANGKUNG', 'Lumajang',
    ARRAY['photo54.jpg', 'photo55.jpg']
),

-- PADANG (kdKec: 121)
(
    'BIZ-032', 'Warung Mie Ayam Berkah', '2026-01-25',
    54.00, 21,
    '08:00:00', '20:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    380000.00, 510000.00,
    10260000.00, 9800000.00,
    0.83, 1.09, 0.85,
    980000.00, 12082400.00,
    0.10, 0.03,
    69, 'MEDIUM',
    'SURV-001', TRUE,
    -8.172300, 113.834500, 'Jl. Padang Raya No. 45', 'PADANG', 'PADANG', 'Lumajang',
    ARRAY['photo56.jpg']
),

-- PASRUJAMBE (kdKec: 131)
(
    'BIZ-033', 'Depot Nasi Campur Ibu Sri', '2026-01-25',
    68.00, 29,
    '07:00:00', '19:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    560000.00, 750000.00,
    15120000.00, 14500000.00,
    0.88, 1.10, 0.87,
    1450000.00, 17879750.00,
    0.10, 0.03,
    72, 'MEDIUM',
    'SURV-002', TRUE,
    -8.267800, 113.123400, 'Jl. Pasrujambe No. 89', 'PASRUJAMBE', 'PASRUJAMBE', 'Lumajang',
    ARRAY['photo57.jpg', 'photo58.jpg']
),

-- KLAKAH (kdKec: 150)
(
    'BIZ-034', 'Restaurant Seafood Klakah', '2026-01-26',
    145.00, 58,
    '11:00:00', '21:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1580000.00, 2280000.00,
    42660000.00, 55500000.00,
    1.09, 1.21, 0.89,
    5550000.00, 68399250.00,
    0.10, 0.03,
    80, 'HIGH',
    'SURV-003', TRUE,
    -8.223400, 113.445600, 'Jl. Klakah Pantai No. 12', 'KLAKAH', 'K L A K A H', 'Lumajang',
    ARRAY['photo59.jpg', 'photo60.jpg']
),

-- KEDUNGJAJANG (kdKec: 151)
(
    'BIZ-035', 'Warung Tahu Campur Pak Mul', '2026-01-26',
    46.00, 18,
    '06:00:00', '16:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    320000.00, 420000.00,
    8640000.00, 8300000.00,
    0.82, 1.08, 0.85,
    830000.00, 10233900.00,
    0.10, 0.03,
    67, 'MEDIUM',
    'SURV-001', TRUE,
    -8.298900, 113.512300, 'Jl. Kedungjajang No. 34', 'KEDUNGJAJANG', 'KEDUNGJAJANG', 'Lumajang',
    ARRAY['photo61.jpg']
),

-- RANUYOSO (kdKec: 160)
(
    'BIZ-036', 'Café Mountain View Ranuyoso', '2026-01-27',
    110.00, 48,
    '08:00:00', '22:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    1150000.00, 1650000.00,
    31050000.00, 43000000.00,
    1.17, 1.19, 0.89,
    4300000.00, 53024500.00,
    0.10, 0.03,
    79, 'MEDIUM',
    'SURV-002', TRUE,
    -8.334500, 113.567800, 'Jl. Wisata Ranuyoso No. 7', 'RANUYOSO', 'RANUYOSO', 'Lumajang',
    ARRAY['photo62.jpg', 'photo63.jpg']
),

-- Additional for LUMAJANG to make it more prominent
(
    'BIZ-037', 'Franchise KFC Lumajang', '2026-01-27',
    250.00, 100,
    '09:00:00', '22:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Transfer'],
    3500000.00, 5200000.00,
    97300000.00, 125000000.00,
    1.25, 1.30, 0.92,
    12500000.00, 154062500.00,
    0.10, 0.03,
    90, 'HIGH',
    'SURV-003', TRUE,
    -8.129500, 113.718900, 'Jl. Basuki Rahmat No. 456', 'CITRODIWANGSAN', 'LUMAJANG', 'Lumajang',
    ARRAY['photo64.jpg', 'photo65.jpg', 'photo66.jpg']
),
(
    'BIZ-038', 'Depot Es Campur Legendaris', '2026-01-28',
    58.00, 24,
    '10:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    480000.00, 650000.00,
    12960000.00, 12500000.00,
    0.86, 1.10, 0.86,
    1250000.00, 15406250.00,
    0.10, 0.03,
    71, 'MEDIUM',
    'SURV-001', TRUE,
    -8.132800, 113.719600, 'Jl. Alun-alun Utara No. 8', 'DENOK', 'LUMAJANG', 'Lumajang',
    ARRAY['photo67.jpg']
),
(
    'BIZ-039', 'Warung Gulai Kambing Khas Aceh', '2026-01-28',
    75.00, 33,
    '09:00:00', '21:00:00',
    'WARUNG_SEDANG', ARRAY['Cash', 'QRIS'],
    720000.00, 980000.00,
    19440000.00, 18800000.00,
    0.91, 1.12, 0.87,
    1880000.00, 23182800.00,
    0.10, 0.03,
    75, 'MEDIUM',
    'SURV-002', TRUE,
    -8.126200, 113.717300, 'Jl. Panglima Sudirman No. 99', 'BLUKON', 'LUMAJANG', 'Lumajang',
    ARRAY['photo68.jpg', 'photo69.jpg']
),
(
    'BIZ-040', 'Restaurant Padang Sederhana', '2026-01-29',
    128.00, 52,
    '08:00:00', '21:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC'],
    1480000.00, 2150000.00,
    39960000.00, 52000000.00,
    1.07, 1.20, 0.90,
    5200000.00, 64098000.00,
    0.10, 0.03,
    81, 'HIGH',
    'SURV-003', TRUE,
    -8.130900, 113.722400, 'Jl. Ahmad Yani No. 178', 'BORENG', 'LUMAJANG', 'Lumajang',
    ARRAY['photo70.jpg', 'photo71.jpg']
);

-- ===============================================
-- SAMPLE OBSERVATION HISTORY
-- ===============================================

INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
-- Observations for BIZ-001 (Warung Makan Sederhana)
(1, '2026-01-05 12:00:00', 'WEEKDAY_PEAK', 45, 2.0, 28000.00,
 '[25000, 32000, 28000, 30000, 27000, 35000, 24000, 29000, 31000, 26000]'::jsonb,
 22.50, 'Lunch hour observation'),
(1, '2026-01-05 15:00:00', 'WEEKDAY_OFFPEAK', 12, 2.0, 18000.00,
 '[15000, 20000, 18000, 17000, 19000, 16000, 21000]'::jsonb,
 6.00, 'Afternoon observation'),
(1, '2026-01-06 18:00:00', 'WEEKEND_PEAK', 68, 2.0, 35000.00,
 '[32000, 38000, 35000, 33000, 37000, 34000, 36000, 35000, 39000, 31000]'::jsonb,
 34.00, 'Weekend dinner observation'),

-- Observations for BIZ-002 (Restaurant Nikmat Rasa)
(2, '2026-01-04 12:30:00', 'WEEKDAY_PEAK', 85, 2.0, 55000.00,
 '[48000, 62000, 55000, 53000, 58000, 51000, 59000, 54000, 56000, 52000]'::jsonb,
 42.50, 'Peak lunch time'),
(2, '2026-01-05 19:00:00', 'WEEKEND_PEAK', 120, 2.0, 68000.00,
 '[65000, 72000, 68000, 67000, 70000, 66000, 71000, 69000, 73000, 64000]'::jsonb,
 60.00, 'Weekend dinner rush'),

-- Observations for BIZ-003 (Café Modern Hits)
(3, '2026-01-03 14:00:00', 'WEEKDAY_OFFPEAK', 32, 2.0, 45000.00,
 '[42000, 48000, 45000, 44000, 46000, 43000, 47000, 45000, 49000, 41000]'::jsonb,
 16.00, 'Afternoon cafe time'),
(3, '2026-01-04 19:00:00', 'WEEKEND_PEAK', 58, 2.0, 52000.00,
 '[48000, 55000, 52000, 51000, 53000, 50000, 54000, 52000, 56000, 49000]'::jsonb,
 29.00, 'Evening weekend crowd'),

-- Observations for BIZ-006 (Resto Seafood Bahari)
(6, '2026-01-08 13:00:00', 'WEEKDAY_PEAK', 95, 2.0, 72000.00,
 '[68000, 75000, 72000, 71000, 73000, 70000, 74000, 72000, 76000, 69000]'::jsonb,
 47.50, 'Lunch seafood lovers'),
(6, '2026-01-09 20:00:00', 'WEEKEND_PEAK', 145, 2.0, 85000.00,
 '[80000, 88000, 85000, 84000, 86000, 83000, 87000, 85000, 89000, 82000]'::jsonb,
 72.50, 'Weekend dinner rush'),

-- Observations for BIZ-011 (Restaurant & Catering Berkah)
(11, '2026-01-13 12:30:00', 'WEEKDAY_PEAK', 110, 2.0, 65000.00,
 '[60000, 68000, 65000, 64000, 66000, 63000, 67000, 65000, 69000, 62000]'::jsonb,
 55.00, 'Busy lunch service'),
(11, '2026-01-14 19:30:00', 'WEEKEND_PEAK', 165, 2.0, 78000.00,
 '[73000, 80000, 78000, 77000, 79000, 76000, 80000, 78000, 81000, 75000]'::jsonb,
 82.50, 'Full capacity weekend dinner');

-- ===============================================
-- VERIFICATION QUERIES
-- ===============================================

-- Check total assessments
SELECT COUNT(*) as total_assessments FROM pbjt_assessments WHERE business_id LIKE 'BIZ-%';

-- Check assessments by kecamatan
SELECT 
    kecamatan,
    COUNT(*) as jumlah_usaha,
    SUM(annual_pbjt) as total_annual_pbjt
FROM pbjt_assessments
WHERE business_id LIKE 'BIZ-%'
GROUP BY kecamatan
ORDER BY kecamatan;

-- Check assessments by kelurahan
SELECT 
    kecamatan,
    kelurahan,
    COUNT(*) as jumlah_usaha,
    AVG(confidence_score) as avg_confidence
FROM pbjt_assessments
WHERE business_id LIKE 'BIZ-%'
GROUP BY kecamatan, kelurahan
ORDER BY kecamatan, kelurahan;

-- Check total observations
SELECT COUNT(*) as total_observations FROM pbjt_observation_history;

-- Full join report
SELECT 
    a.business_id,
    a.business_name,
    a.kecamatan,
    a.kelurahan,
    a.annual_pbjt,
    a.confidence_level,
    COUNT(o.id) as observation_count
FROM pbjt_assessments a
LEFT JOIN pbjt_observation_history o ON a.id = o.assessment_id
WHERE a.business_id LIKE 'BIZ-%'
GROUP BY a.id, a.business_id, a.business_name, a.kecamatan, a.kelurahan, a.annual_pbjt, a.confidence_level
ORDER BY a.kecamatan, a.kelurahan;

-- ===============================================
-- NOTES
-- ===============================================
-- This seeder includes 40 sample assessments spread across 18 different kecamatan in Lumajang
-- Kecamatan covered (with correct kode):
-- 1. LUMAJANG (060) - 7 assessments: TOMPOKERSAN, ROGOTRUNAN, JOGOYUDAN, DITOTRUNAN, LABRUK LOR, CITRODIWANGSAN, DENOK, BLUKON, BORENG
-- 2. CANDIPURO (030) - 4 assessments: YUGOSARI, SUMBER REJO, J A R I T, SUMBERWULUH, PENANGGAL
-- 3. JATIROTO (100) - 2 assessments
-- 4. PASIRIAN (040) - 2 assessments
-- 5. PRONOJIWO (020) - 2 assessments
-- 6. T E M P E H (050) - 3 assessments
-- 7. SUKODONO (120) - 2 assessments
-- 8. TEMPUR SARI (010) - 2 assessments
-- 9. YOSOWILANGUN (090) - 2 assessments
-- 10. SENDURO (130) - 2 assessments
-- 11. K U N I R (080) - 1 assessment
-- 12. SUMBER SUKO (061) - 1 assessment
-- 13. T E K U N G (070) - 1 assessment
-- 14. ROWOKANGKUNG (101) - 1 assessment
-- 15. PADANG (121) - 1 assessment
-- 16. PASRUJAMBE (131) - 1 assessment
-- 17. K L A K A H (150) - 1 assessment
-- 18. KEDUNGJAJANG (151) - 1 assessment
-- 19. RANUYOSO (160) - 1 assessment

-- All kecamatan and kelurahan names match the master data from Bidang API
-- This ensures consistency between:
-- - Map statistics display
-- - Form dropdown values
-- - Database records
-- ===============================================
