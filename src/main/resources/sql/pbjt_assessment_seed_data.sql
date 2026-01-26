-- ===============================================
-- PBJT Assessment System - Additional Seed Data
-- More comprehensive test data for development
-- ===============================================

-- Clear existing data if needed (be careful in production!)
-- TRUNCATE TABLE pbjt_observation_history CASCADE;
-- TRUNCATE TABLE pbjt_assessments RESTART IDENTITY CASCADE;

-- ===============================================
-- ADDITIONAL ASSESSMENTS (10 more businesses)
-- ===============================================

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
-- 4. Bar Premium
(
    'BIZ-004', 'Sky Lounge Bar', '2025-12-28',
    250.00, 80,
    '17:00:00', '02:00:00',
    'BAR_LOUNGE', ARRAY['Cash', 'QRIS', 'EDC', 'Debit'],
    3200000.00, 5800000.00,
    89600000.00, 125200000.00,
    1.50, 1.35, 0.85,
    12520000.00, 154296800.00,
    0.10, 0.03,
    88, 'HIGH',
    'SURV-003', TRUE,
    -8.130000, 113.722000, 'Jl. Panglima Sudirman No. 201', 'Tompokersan', 'Lumajang', 'Lumajang',
    ARRAY['bar1.jpg', 'bar2.jpg', 'bar3.jpg']
),
-- 5. Warung Kopi Tradisional
(
    'BIZ-005', 'Warung Kopi Pak Haji', '2026-01-02',
    45.00, 15,
    '05:00:00', '18:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    280000.00, 320000.00,
    6880000.00, 6200000.00,
    0.80, 1.00, 0.92,
    620000.00, 7639200.00,
    0.10, 0.03,
    65, 'MEDIUM',
    'SURV-002', TRUE,
    -8.124500, 113.715000, 'Jl. KH Hasyim Ashari No. 12', 'Kepuharjo', 'Lumajang', 'Lumajang',
    ARRAY['kopi1.jpg', 'kopi2.jpg']
),
-- 6. Fast Food Chain
(
    'BIZ-006', 'Fried Chicken Express', '2025-12-30',
    150.00, 60,
    '09:00:00', '22:00:00',
    'FAST_FOOD', ARRAY['Cash', 'QRIS', 'EDC', 'Debit'],
    2150000.00, 3280000.00,
    57450000.00, 71900000.00,
    1.15, 1.20, 0.88,
    7190000.00, 88588800.00,
    0.10, 0.03,
    82, 'HIGH',
    'SURV-001', TRUE,
    -8.128000, 113.719000, 'Jl. Jendral Ahmad Yani No. 45', 'Rogotrunan', 'Lumajang', 'Lumajang',
    ARRAY['fastfood1.jpg', 'fastfood2.jpg', 'fastfood3.jpg']
),
-- 7. Bakery & Pastry
(
    'BIZ-007', 'Roti Manis Bakery', '2026-01-01',
    85.00, 20,
    '06:00:00', '20:00:00',
    'BAKERY', ARRAY['Cash', 'QRIS'],
    780000.00, 1150000.00,
    18580000.00, 20400000.00,
    1.00, 1.10, 0.90,
    2040000.00, 25142400.00,
    0.10, 0.03,
    75, 'MEDIUM',
    'SURV-002', FALSE,
    -8.126000, 113.717500, 'Jl. Veteran No. 78', 'Kepuharjo', 'Lumajang', 'Lumajang',
    ARRAY['bakery1.jpg', 'bakery2.jpg']
),
-- 8. Seafood Restaurant
(
    'BIZ-008', 'Seafood Bahari Jaya', '2025-12-29',
    200.00, 90,
    '10:00:00', '23:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Debit', 'Transfer'],
    2850000.00, 4250000.00,
    71850000.00, 95000000.00,
    1.25, 1.30, 0.88,
    9500000.00, 117090000.00,
    0.10, 0.03,
    90, 'HIGH',
    'SURV-003', TRUE,
    -8.132000, 113.721500, 'Jl. Gatot Subroto No. 156', 'Tompokersan', 'Lumajang', 'Lumajang',
    ARRAY['seafood1.jpg', 'seafood2.jpg', 'seafood3.jpg', 'seafood4.jpg']
),
-- 9. Warung Pecel Lele
(
    'BIZ-009', 'Pecel Lele Sari Rasa', '2026-01-06',
    60.00, 20,
    '10:00:00', '22:00:00',
    'WARUNG_KECIL', ARRAY['Cash', 'QRIS'],
    520000.00, 780000.00,
    12480000.00, 13000000.00,
    0.85, 1.05, 0.92,
    1300000.00, 16023600.00,
    0.10, 0.03,
    70, 'MEDIUM',
    'SURV-001', TRUE,
    -8.127500, 113.718500, 'Jl. Diponegoro No. 23', 'Rogotrunan', 'Lumajang', 'Lumajang',
    ARRAY['pecel1.jpg', 'pecel2.jpg']
),
-- 10. Bubble Tea Shop
(
    'BIZ-010', 'Bubble Tea Paradise', '2025-12-27',
    70.00, 25,
    '10:00:00', '22:00:00',
    'CAFE_MODERN', ARRAY['Cash', 'QRIS', 'EDC'],
    950000.00, 1550000.00,
    21850000.00, 28500000.00,
    1.18, 1.15, 0.90,
    2850000.00, 35118300.00,
    0.10, 0.03,
    76, 'MEDIUM',
    'SURV-002', FALSE,
    -8.125500, 113.719500, 'Jl. Gajah Mada No. 34', 'Kepuharjo', 'Lumajang', 'Lumajang',
    ARRAY['bubble1.jpg', 'bubble2.jpg']
),
-- 11. Pizza Restaurant
(
    'BIZ-011', 'Pizza Italia', '2026-01-07',
    140.00, 55,
    '11:00:00', '23:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Debit'],
    1650000.00, 2480000.00,
    39750000.00, 52000000.00,
    1.12, 1.18, 0.89,
    5200000.00, 64092000.00,
    0.10, 0.03,
    81, 'HIGH',
    'SURV-003', TRUE,
    -8.129500, 113.720500, 'Jl. Soekarno Hatta No. 89', 'Tompokersan', 'Lumajang', 'Lumajang',
    ARRAY['pizza1.jpg', 'pizza2.jpg', 'pizza3.jpg']
),
-- 12. Nasi Goreng Street Food
(
    'BIZ-012', 'Nasi Goreng Abang Jago', '2026-01-08',
    25.00, 10,
    '18:00:00', '02:00:00',
    'WARUNG_KECIL', ARRAY['Cash'],
    350000.00, 480000.00,
    8150000.00, 7500000.00,
    0.80, 0.95, 0.95,
    750000.00, 9245700.00,
    0.10, 0.03,
    62, 'MEDIUM',
    'SURV-001', FALSE,
    -8.126700, 113.716800, 'Jl. Imam Bonjol No. 5 (Depan Alun-Alun)', 'Rogotrunan', 'Lumajang', 'Lumajang',
    ARRAY['nasgor1.jpg']
),
-- 13. Upscale Dining
(
    'BIZ-013', 'Le Jardin Fine Dining', '2025-12-26',
    300.00, 65,
    '11:00:00', '23:00:00',
    'RESTAURANT', ARRAY['Cash', 'QRIS', 'EDC', 'Debit', 'Credit'],
    3800000.00, 6200000.00,
    97600000.00, 138000000.00,
    1.40, 1.40, 0.86,
    13800000.00, 170154600.00,
    0.10, 0.03,
    92, 'HIGH',
    'SURV-003', TRUE,
    -8.131500, 113.723000, 'Jl. Pahlawan No. 234 (Hotel Zone)', 'Tompokersan', 'Lumajang', 'Lumajang',
    ARRAY['finedining1.jpg', 'finedining2.jpg', 'finedining3.jpg', 'finedining4.jpg']
);

-- ===============================================
-- ADDITIONAL OBSERVATION HISTORIES
-- ===============================================

-- Observations for BIZ-004 (Sky Lounge Bar)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(4, '2025-12-28 20:00:00', 'WEEKDAY_PEAK', 52, 2.5, 185000.00,
 '[175000, 195000, 180000, 190000, 182000, 188000, 177000, 193000, 184000, 186000]'::jsonb,
 20.80, 'Friday night crowd'),
(4, '2025-12-28 23:30:00', 'WEEKEND_PEAK', 78, 2.0, 220000.00,
 '[210000, 230000, 215000, 225000, 218000, 228000, 212000, 235000, 220000, 222000]'::jsonb,
 39.00, 'Late night peak'),
(4, '2025-12-29 21:00:00', 'WEEKEND_PEAK', 88, 2.5, 245000.00,
 '[235000, 255000, 240000, 250000, 242000, 252000, 238000, 258000, 245000, 248000]'::jsonb,
 35.20, 'Saturday prime time');

-- Observations for BIZ-005 (Warung Kopi Pak Haji)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(5, '2026-01-02 06:30:00', 'WEEKDAY_PEAK', 38, 2.0, 15000.00,
 '[12000, 18000, 15000, 14000, 16000, 13000, 17000, 15000, 16000, 14000]'::jsonb,
 19.00, 'Morning coffee rush'),
(5, '2026-01-02 10:00:00', 'WEEKDAY_OFFPEAK', 15, 2.0, 12000.00,
 '[10000, 14000, 12000, 11000, 13000, 12000, 11000, 13000, 12000, 11000]'::jsonb,
 7.50, 'Mid-morning slow period');

-- Observations for BIZ-006 (Fried Chicken Express)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(6, '2025-12-30 12:30:00', 'WEEKDAY_PEAK', 75, 2.0, 75000.00,
 '[68000, 82000, 75000, 73000, 78000, 72000, 80000, 74000, 76000, 71000]'::jsonb,
 37.50, 'Lunch hour rush'),
(6, '2025-12-30 18:30:00', 'WEEKDAY_PEAK', 68, 2.0, 72000.00,
 '[65000, 78000, 72000, 70000, 75000, 68000, 77000, 71000, 73000, 69000]'::jsonb,
 34.00, 'Dinner time'),
(6, '2025-12-31 13:00:00', 'WEEKEND_PEAK', 95, 2.0, 85000.00,
 '[78000, 92000, 85000, 83000, 88000, 82000, 90000, 84000, 86000, 81000]'::jsonb,
 47.50, 'Weekend family dining');

-- Observations for BIZ-007 (Roti Manis Bakery)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(7, '2026-01-01 08:00:00', 'HOLIDAY', 52, 2.0, 48000.00,
 '[42000, 54000, 48000, 46000, 50000, 45000, 52000, 47000, 49000, 44000]'::jsonb,
 26.00, 'New Year morning shopping'),
(7, '2026-01-01 15:00:00', 'HOLIDAY', 45, 2.0, 42000.00,
 '[38000, 46000, 42000, 40000, 44000, 39000, 45000, 41000, 43000, 38000]'::jsonb,
 22.50, 'Afternoon snack time');

-- Observations for BIZ-008 (Seafood Bahari Jaya)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(8, '2025-12-29 13:00:00', 'WEEKEND_PEAK', 98, 2.5, 125000.00,
 '[115000, 135000, 125000, 122000, 128000, 118000, 132000, 123000, 127000, 120000]'::jsonb,
 39.20, 'Sunday lunch family gathering'),
(8, '2025-12-29 19:00:00', 'WEEKEND_PEAK', 112, 2.5, 145000.00,
 '[135000, 155000, 145000, 142000, 148000, 138000, 152000, 143000, 147000, 140000]'::jsonb,
 44.80, 'Weekend dinner peak');

-- Observations for BIZ-009 (Pecel Lele)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(9, '2026-01-06 18:30:00', 'WEEKDAY_PEAK', 42, 2.0, 32000.00,
 '[28000, 36000, 32000, 30000, 34000, 29000, 35000, 31000, 33000, 30000]'::jsonb,
 21.00, 'Dinner time observation'),
(9, '2026-01-06 20:30:00', 'WEEKDAY_PEAK', 38, 1.5, 30000.00,
 '[26000, 34000, 30000, 28000, 32000, 27000, 33000, 29000, 31000, 28000]'::jsonb,
 25.33, 'Late evening customers');

-- Observations for BIZ-010 (Bubble Tea Paradise)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(10, '2025-12-27 14:00:00', 'WEEKDAY_OFFPEAK', 35, 2.0, 38000.00,
 '[32000, 44000, 38000, 36000, 40000, 34000, 42000, 37000, 39000, 35000]'::jsonb,
 17.50, 'Afternoon customers'),
(10, '2025-12-27 19:00:00', 'WEEKDAY_PEAK', 58, 2.0, 42000.00,
 '[38000, 46000, 42000, 40000, 44000, 39000, 45000, 41000, 43000, 40000]'::jsonb,
 29.00, 'Evening young crowd');

-- Observations for BIZ-011 (Pizza Italia)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(11, '2026-01-07 13:00:00', 'WEEKDAY_PEAK', 62, 2.0, 88000.00,
 '[82000, 94000, 88000, 86000, 90000, 84000, 92000, 87000, 89000, 85000]'::jsonb,
 31.00, 'Lunch families'),
(11, '2026-01-07 19:30:00', 'WEEKDAY_PEAK', 72, 2.5, 95000.00,
 '[88000, 102000, 95000, 93000, 98000, 90000, 100000, 94000, 96000, 92000]'::jsonb,
 28.80, 'Dinner gathering');

-- Observations for BIZ-012 (Nasi Goreng Abang Jago)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(12, '2026-01-08 20:00:00', 'WEEKDAY_PEAK', 28, 2.0, 22000.00,
 '[18000, 26000, 22000, 20000, 24000, 19000, 25000, 21000, 23000, 20000]'::jsonb,
 14.00, 'Evening street food customers'),
(12, '2026-01-08 23:00:00', 'WEEKDAY_PEAK', 35, 1.5, 24000.00,
 '[20000, 28000, 24000, 22000, 26000, 21000, 27000, 23000, 25000, 22000]'::jsonb,
 23.33, 'Late night crowd');

-- Observations for BIZ-013 (Le Jardin Fine Dining)
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(13, '2025-12-26 13:00:00', 'WEEKDAY_PEAK', 48, 3.0, 285000.00,
 '[265000, 305000, 285000, 278000, 292000, 270000, 298000, 282000, 288000, 275000]'::jsonb,
 16.00, 'Business lunch meetings'),
(13, '2025-12-26 20:00:00', 'WEEKDAY_PEAK', 62, 3.0, 325000.00,
 '[305000, 345000, 325000, 318000, 332000, 310000, 338000, 322000, 328000, 315000]'::jsonb,
 20.67, 'Fine dining dinner service'),
(13, '2025-12-27 19:30:00', 'WEEKEND_PEAK', 75, 3.0, 365000.00,
 '[345000, 385000, 365000, 358000, 372000, 350000, 378000, 362000, 368000, 355000]'::jsonb,
 25.00, 'Weekend special occasion dining');

-- ===============================================
-- VERIFICATION QUERIES
-- ===============================================

-- Total assessments count
SELECT COUNT(*) as total_assessments FROM pbjt_assessments;

-- Total observations count
SELECT COUNT(*) as total_observations FROM pbjt_observation_history;

-- Summary by business type
SELECT 
    business_type,
    COUNT(*) as count,
    ROUND(AVG(annual_pbjt)::numeric, 0) as avg_annual_pbjt,
    ROUND(AVG(confidence_score)::numeric, 1) as avg_confidence
FROM pbjt_assessments
GROUP BY business_type
ORDER BY avg_annual_pbjt DESC;

-- Top 5 businesses by annual PBJT
SELECT 
    business_id,
    business_name,
    business_type,
    TO_CHAR(annual_pbjt, 'Rp999,999,999,999') as annual_pbjt,
    confidence_level
FROM pbjt_assessments
ORDER BY annual_pbjt DESC
LIMIT 5;

-- Businesses with most observations
SELECT 
    a.business_id,
    a.business_name,
    COUNT(o.id) as observation_count,
    ROUND(AVG(o.visitors_per_hour)::numeric, 2) as avg_visitors_per_hour
FROM pbjt_assessments a
LEFT JOIN pbjt_observation_history o ON a.id = o.assessment_id
GROUP BY a.id, a.business_id, a.business_name
ORDER BY observation_count DESC;

-- Confidence level distribution
SELECT 
    confidence_level,
    COUNT(*) as count,
    ROUND(AVG(confidence_score)::numeric, 1) as avg_score
FROM pbjt_assessments
GROUP BY confidence_level
ORDER BY avg_score DESC;

-- Revenue ranges
SELECT 
    CASE 
        WHEN annual_pbjt < 10000000 THEN '< 10 Juta'
        WHEN annual_pbjt < 50000000 THEN '10-50 Juta'
        WHEN annual_pbjt < 100000000 THEN '50-100 Juta'
        ELSE '> 100 Juta'
    END as revenue_range,
    COUNT(*) as count
FROM pbjt_assessments
GROUP BY revenue_range
ORDER BY MIN(annual_pbjt);

-- Assessment by surveyor
SELECT 
    surveyor_id,
    COUNT(*) as assessment_count,
    COUNT(CASE WHEN taxpayer_signed = TRUE THEN 1 END) as signed_count
FROM pbjt_assessments
GROUP BY surveyor_id
ORDER BY assessment_count DESC;

-- ===============================================
-- END OF SEED DATA
-- ===============================================
