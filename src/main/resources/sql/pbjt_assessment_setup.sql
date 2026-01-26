-- ===============================================
-- PBJT Assessment System Database Schema
-- PostgreSQL 14+ for Leaflet-Geo Integration
-- Database: pbjt_assessment_db (localhost)
-- ===============================================

-- Create database (run as postgres superuser first)
-- CREATE DATABASE pbjt_assessment_db;
-- \c pbjt_assessment_db;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===============================================
-- MAIN TABLES
-- ===============================================

-- Table: pbjt_assessments
CREATE TABLE IF NOT EXISTS pbjt_assessments (
    id BIGSERIAL PRIMARY KEY,
    business_id VARCHAR(50) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    assessment_date DATE NOT NULL,
    
    -- Profile data
    building_area NUMERIC(10, 2),
    seating_capacity INTEGER,
    operating_hours_start TIME,
    operating_hours_end TIME,
    business_type VARCHAR(50),
    payment_methods TEXT[],
    
    -- Observation data (stored as JSONB for flexibility)
    observations JSONB,
    
    -- Calculation results
    daily_revenue_weekday NUMERIC(15, 2),
    daily_revenue_weekend NUMERIC(15, 2),
    monthly_revenue_raw NUMERIC(15, 2),
    monthly_revenue_adjusted NUMERIC(15, 2),
    
    -- Adjustment factors
    business_type_coefficient NUMERIC(4, 2),
    location_score NUMERIC(4, 2),
    operational_rate NUMERIC(4, 2),
    
    -- Tax calculations
    monthly_pbjt NUMERIC(15, 2),
    annual_pbjt NUMERIC(15, 2),
    tax_rate NUMERIC(4, 2) DEFAULT 0.10,
    inflation_rate NUMERIC(4, 2) DEFAULT 0.03,
    
    -- Confidence scoring
    confidence_score INTEGER,
    confidence_level VARCHAR(20) CHECK (confidence_level IN ('LOW', 'MEDIUM', 'HIGH')),
    
    -- Validation data as JSONB
    validation_data JSONB,
    
    -- Audit trail
    surveyor_id VARCHAR(50),
    verified_by VARCHAR(50),
    taxpayer_signed BOOLEAN DEFAULT FALSE,
    
    -- Location data (simplified - no PostGIS required)
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    address TEXT,
    kelurahan VARCHAR(100),
    kecamatan VARCHAR(100),
    kabupaten VARCHAR(100),
    
    -- Tax object identification (from SIMATDA)
    tax_object_id VARCHAR(50),
    tax_object_number VARCHAR(50), -- NOP (Nomor Objek Pajak)
    
    -- Supporting documents
    photo_urls TEXT[],
    supporting_doc_url TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_coordinates CHECK (
        latitude BETWEEN -90 AND 90 AND
        longitude BETWEEN -180 AND 180
    ),
    CONSTRAINT valid_capacity CHECK (seating_capacity > 0),
    CONSTRAINT valid_confidence CHECK (confidence_score BETWEEN 0 AND 100)
);

-- Table: pbjt_observation_history
CREATE TABLE IF NOT EXISTS pbjt_observation_history (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    observation_date TIMESTAMP NOT NULL,
    day_type VARCHAR(30) NOT NULL CHECK (day_type IN ('WEEKDAY_PEAK', 'WEEKDAY_OFFPEAK', 'WEEKEND_PEAK', 'HOLIDAY')),
    visitors INTEGER NOT NULL,
    duration_hours NUMERIC(3, 1) NOT NULL,
    avg_transaction NUMERIC(10, 2) NOT NULL,
    sample_transactions JSONB,
    visitors_per_hour NUMERIC(10, 2),
    notes TEXT,
    
    CONSTRAINT fk_assessment
        FOREIGN KEY (assessment_id)
        REFERENCES pbjt_assessments(id)
        ON DELETE CASCADE,
    
    CONSTRAINT valid_visitors CHECK (visitors > 0),
    CONSTRAINT valid_duration CHECK (duration_hours > 0)
);

-- ===============================================
-- INDEXES FOR PERFORMANCE
-- ===============================================

-- Indexes on pbjt_assessments
CREATE INDEX idx_business_id ON pbjt_assessments(business_id);
CREATE INDEX idx_business_type ON pbjt_assessments(business_type);
CREATE INDEX idx_confidence_level ON pbjt_assessments(confidence_level);
CREATE INDEX idx_assessment_date ON pbjt_assessments(assessment_date);
CREATE INDEX idx_kabupaten ON pbjt_assessments(kabupaten);
CREATE INDEX idx_kecamatan ON pbjt_assessments(kecamatan);
CREATE INDEX idx_created_at ON pbjt_assessments(created_at DESC);
CREATE INDEX idx_lat_lng ON pbjt_assessments(latitude, longitude);

-- JSONB indexes for faster queries
CREATE INDEX idx_observations_gin ON pbjt_assessments USING GIN(observations);
CREATE INDEX idx_validation_data_gin ON pbjt_assessments USING GIN(validation_data);

-- Indexes on pbjt_observation_history
CREATE INDEX idx_obs_assessment_id ON pbjt_observation_history(assessment_id);
CREATE INDEX idx_obs_day_type ON pbjt_observation_history(day_type);
CREATE INDEX idx_obs_date ON pbjt_observation_history(observation_date);

-- ===============================================
-- TRIGGERS
-- ===============================================

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_pbjt_assessments_updated_at
    BEFORE UPDATE ON pbjt_assessments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ===============================================
-- SAMPLE DATA FOR TESTING
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
    -8.125000, 113.718750, 'Jl. Pahlawan No. 45', 'Tompokersan', 'Lumajang', 'Lumajang',
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
    -8.133333, 113.716667, 'Jl. Basuki Rahmat No. 123', 'Rogotrunan', 'Lumajang', 'Lumajang',
    ARRAY['photo3.jpg', 'photo4.jpg', 'photo5.jpg']
),
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
    -8.126944, 113.720833, 'Jl. Ahmad Yani No. 88', 'Kepuharjo', 'Lumajang', 'Lumajang',
    ARRAY['photo6.jpg', 'photo7.jpg']
);

-- Insert sample observation histories
INSERT INTO pbjt_observation_history (
    assessment_id, observation_date, day_type,
    visitors, duration_hours, avg_transaction,
    sample_transactions, visitors_per_hour, notes
) VALUES
(1, '2026-01-05 12:00:00', 'WEEKDAY_PEAK', 45, 2.0, 28000.00,
 '[25000, 32000, 28000, 30000, 27000, 35000, 24000, 29000, 31000, 26000]'::jsonb,
 22.50, 'Lunch hour observation'),
(1, '2026-01-05 15:00:00', 'WEEKDAY_OFFPEAK', 12, 2.0, 18000.00,
 '[15000, 20000, 18000, 17000, 19000, 16000, 21000]'::jsonb,
 6.00, 'Afternoon observation'),
(1, '2026-01-06 18:00:00', 'WEEKEND_PEAK', 68, 2.0, 35000.00,
 '[32000, 38000, 35000, 33000, 37000, 34000, 36000, 35000, 39000, 31000]'::jsonb,
 34.00, 'Weekend dinner observation'),
(2, '2026-01-04 12:30:00', 'WEEKDAY_PEAK', 85, 2.0, 55000.00,
 '[48000, 62000, 55000, 53000, 58000, 51000, 59000, 54000, 56000, 52000]'::jsonb,
 42.50, 'Peak lunch time'),
(2, '2026-01-05 19:00:00', 'WEEKEND_PEAK', 120, 2.0, 68000.00,
 '[65000, 72000, 68000, 67000, 70000, 66000, 71000, 69000, 73000, 64000]'::jsonb,
 60.00, 'Weekend dinner rush');

-- ===============================================
-- VERIFICATION QUERIES
-- ===============================================

-- Verify data
SELECT COUNT(*) as assessment_count FROM pbjt_assessments;
SELECT COUNT(*) as observation_count FROM pbjt_observation_history;

-- Test query with join
SELECT 
    a.business_name,
    a.business_type,
    a.annual_pbjt,
    COUNT(o.id) as observation_count
FROM pbjt_assessments a
LEFT JOIN pbjt_observation_history o ON a.id = o.assessment_id
GROUP BY a.id, a.business_name, a.business_type, a.annual_pbjt;
