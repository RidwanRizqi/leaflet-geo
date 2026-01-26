-- ===============================================
-- Script untuk Update kd_kec di pbjt_assessments
-- Berdasarkan mapping dari SISMIOP Oracle
-- ===============================================

-- Mapping Kecamatan Lumajang (KD_PROPINSI=35, KD_DATI2=08)
-- Data dari REF_KECAMATAN Oracle SISMIOP

-- Update kd_kec berdasarkan nama kecamatan
UPDATE pbjt_assessments SET kd_kec = '060' WHERE UPPER(kecamatan) = 'LUMAJANG';
UPDATE pbjt_assessments SET kd_kec = '010' WHERE UPPER(kecamatan) = 'KUNIR';
UPDATE pbjt_assessments SET kd_kec = '020' WHERE UPPER(kecamatan) = 'CANDIPURO';
UPDATE pbjt_assessments SET kd_kec = '030' WHERE UPPER(kecamatan) = 'PRONOJIWO';
UPDATE pbjt_assessments SET kd_kec = '040' WHERE UPPER(kecamatan) = 'YOSOWILANGUN';
UPDATE pbjt_assessments SET kd_kec = '050' WHERE UPPER(kecamatan) = 'TEKUNG';
UPDATE pbjt_assessments SET kd_kec = '070' WHERE UPPER(kecamatan) = 'TEMPURSARI';
UPDATE pbjt_assessments SET kd_kec = '080' WHERE UPPER(kecamatan) = 'SENDURO';
UPDATE pbjt_assessments SET kd_kec = '090' WHERE UPPER(kecamatan) = 'GUCIALIT';
UPDATE pbjt_assessments SET kd_kec = '100' WHERE UPPER(kecamatan) = 'KLAKAH';
UPDATE pbjt_assessments SET kd_kec = '110' WHERE UPPER(kecamatan) = 'SUKODONO';
UPDATE pbjt_assessments SET kd_kec = '120' WHERE UPPER(kecamatan) = 'SUMBERSUKO';
UPDATE pbjt_assessments SET kd_kec = '130' WHERE UPPER(kecamatan) = 'JATIROTO';
UPDATE pbjt_assessments SET kd_kec = '140' WHERE UPPER(kecamatan) = 'RANDUAGUNG';
UPDATE pbjt_assessments SET kd_kec = '150' WHERE UPPER(kecamatan) = 'PADANG';
UPDATE pbjt_assessments SET kd_kec = '160' WHERE UPPER(kecamatan) = 'KEDUNGJAJANG';
UPDATE pbjt_assessments SET kd_kec = '170' WHERE UPPER(kecamatan) = 'PASIRIAN';
UPDATE pbjt_assessments SET kd_kec = '180' WHERE UPPER(kecamatan) = 'RANUYOSO';
UPDATE pbjt_assessments SET kd_kec = '190' WHERE UPPER(kecamatan) = 'ROWOKANGKUNG';
UPDATE pbjt_assessments SET kd_kec = '200' WHERE UPPER(kecamatan) = 'TEMPEH';
UPDATE pbjt_assessments SET kd_kec = '210' WHERE UPPER(kecamatan) = 'PASRUJAMBE';

-- Verify update
SELECT 
    kecamatan,
    kd_kec,
    COUNT(*) as total_records
FROM pbjt_assessments
WHERE kecamatan IS NOT NULL
GROUP BY kecamatan, kd_kec
ORDER BY kecamatan;

-- Show summary
SELECT 
    CASE 
        WHEN kd_kec IS NULL THEN 'Missing kd_kec'
        ELSE 'Has kd_kec'
    END as status,
    COUNT(*) as total
FROM pbjt_assessments
GROUP BY 
    CASE 
        WHEN kd_kec IS NULL THEN 'Missing kd_kec'
        ELSE 'Has kd_kec'
    END;
