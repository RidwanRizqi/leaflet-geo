-- Final comprehensive fix for all remaining mismatches
-- The issue is that some records have kecamatan names but wrong kd_kec codes

-- Fix CANDIPURO records - should be kd_kec = '030'
UPDATE pbjt_assessments SET kd_kec = '030', kd_kel = '003' WHERE kelurahan = 'CANDIPURO' AND kd_kec = '020';
UPDATE pbjt_assessments SET kd_kec = '030', kd_kel = '002' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'JARIT' AND kd_kec = '020';
UPDATE pbjt_assessments SET kd_kec = '030', kd_kel = '007' WHERE kelurahan = 'PENANGGAL' AND kd_kec = '020';
UPDATE pbjt_assessments SET kd_kec = '030', kd_kel = '001' WHERE kelurahan = 'YUGOSARI' AND kd_kec = '020';

-- Fix KUNIR record - should be kd_kec = '080'
UPDATE pbjt_assessments SET kd_kec = '080', kd_kel = '007' WHERE kelurahan = 'KUNIR' AND kd_kec = '010';

-- Fix PRONOJIWO records - should be kd_kec = '020'
UPDATE pbjt_assessments SET kd_kec = '020', kd_kel = '006' WHERE kelurahan = 'PRONOJIWO' AND kd_kec = '030';
UPDATE pbjt_assessments SET kd_kec = '020', kd_kel = '002' WHERE kelurahan = 'TAMANAYU' AND kd_kec = '030';

-- Fix SUKODONO records - should be kd_kec = '120'
UPDATE pbjt_assessments SET kd_kec = '120' WHERE kelurahan = 'SUKODONO' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '006' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERREJO' AND kd_kec = '110';

-- Now that kd_kec is correct, set kd_kel for SUKODONO (no specific kel code in REF_KELURAHAN for main kec name)
-- Actually SUKODONO kecamatan doesn't have a kelurahan with same name

-- Fix TEMPEH records - should be kd_kec = '050'
UPDATE pbjt_assessments SET kd_kec = '050' WHERE UPPER(REPLACE(kecamatan, ' ', '')) = 'TEMPEH' AND kd_kec = '200';

-- Fix YOSOWILANGUN/KEBONSARI - should be kd_kec = '090', kd_kel = '005'
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '005' WHERE kelurahan = 'KEBONSARI' AND kd_kec = '040';

-- Now re-check for any kelurahan-specific matches based on corrected kd_kec
-- SUKODONO - REF_KELURAHAN doesn't show a kelurahan called "SUKODONO" in kd_kec 120
-- Let's check what we have after these updates

SELECT 
    COUNT(*) as total_records,
    COUNT(kd_kec) as with_kd_kec,
    COUNT(kd_kel) as with_kd_kel,
    COUNT(*) - COUNT(kd_kel) as missing_kd_kel
FROM pbjt_assessments;

-- Show remaining records without kd_kel
SELECT id, kecamatan, kelurahan, kd_kec, kd_kel 
FROM pbjt_assessments 
WHERE kd_kel IS NULL 
ORDER BY kecamatan, kelurahan;
