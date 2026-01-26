-- Final manual fixes for records where kelurahan name = kecamatan name but no matching entry in REF_KELURAHAN

-- For SUKODONO kelurahan in SUKODONO kecamatan - not in REF_KELURAHAN
-- We'll leave kd_kel as NULL since there's no matching kelurahan code

-- For SUMBEREJO in kd_kec 110 - should be 120, kd_kel = 006
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '006' WHERE id = 91;

-- For TEMPEH kelurahan in TEMPEH kecamatan - not in REF_KELURAHAN as "TEMPEH" alone
-- There are TEMPEHLOR (007), TEMPEHTENGAH (005), TEMPEHKIDUL (003)
-- We'll leave these as NULL or we could default to TEMPEHTENGAH (005) as it's the "central" one
-- Let's leave them NULL for accuracy

-- Final summary
SELECT 
    COUNT(*) as total_records,
    COUNT(kd_kec) as with_kd_kec,
    COUNT(kd_kel) as with_kd_kel,
    COUNT(*) - COUNT(kd_kel) as missing_kd_kel,
    ROUND(100.0 * COUNT(kd_kel) / COUNT(*), 2) as percentage_complete
FROM pbjt_assessments;

-- Show distribution by kecamatan
SELECT 
    kd_kec,
    kecamatan,
    COUNT(*) as total,
    COUNT(kd_kel) as with_kd_kel,
    COUNT(*) - COUNT(kd_kel) as missing_kd_kel
FROM pbjt_assessments 
WHERE kd_kec IS NOT NULL
GROUP BY kd_kec, kecamatan
ORDER BY kd_kec;

-- Show remaining records without kd_kel
SELECT id, kecamatan, kelurahan, kd_kec, kd_kel 
FROM pbjt_assessments 
WHERE kd_kel IS NULL 
ORDER BY kecamatan, kelurahan;
