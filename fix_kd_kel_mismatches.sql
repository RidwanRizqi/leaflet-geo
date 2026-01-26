-- Fix kelurahan names that don't match exactly with SISMIOP
-- These are mostly variations with spaces that need to be handled

-- Fix "SUMBER REJO" -> should match CANDIPURO kd_kel = 004
UPDATE pbjt_assessments SET kd_kel = '004' WHERE kd_kec = '020' AND UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERREJO';
UPDATE pbjt_assessments SET kd_kel = '004' WHERE kd_kec = '030' AND UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERREJO';

-- Fix "SUMBER WULUH" and "SUMBERWULUH" -> CANDIPURO kd_kel = 005
UPDATE pbjt_assessments SET kd_kel = '005' WHERE kd_kec = '020' AND UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERWULUH';
UPDATE pbjt_assessments SET kd_kel = '005' WHERE kd_kec = '030' AND UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERWULUH';

-- Fix wrong kecamatan assignments - these kelurahan are in wrong kecamatan codes
-- DOROGOWOK, KARANGLO, KUNIR LOR should be in kd_kec 080 (KUNIR), not 010
UPDATE pbjt_assessments SET kd_kec = '080', kd_kel = '011' WHERE kelurahan = 'DOROGOWOK' AND kd_kec = '010';
UPDATE pbjt_assessments SET kd_kec = '080', kd_kel = '009' WHERE kelurahan = 'KARANGLO' AND kd_kec = '010';
UPDATE pbjt_assessments SET kd_kec = '080', kd_kel = '007' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'KUNIRLOR' AND kd_kec = '010';

-- YOSOWILANGUN related - should be in kd_kec 090
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '001' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'YOSOWILANGUNLOR' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '002' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'YOSOWILANGUNKIDUL' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '009' WHERE kelurahan = 'KARANGREJO' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '010' WHERE kelurahan = 'KRATON' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '007' WHERE kelurahan = 'KRAI' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kec = '090', kd_kel = '011' WHERE kelurahan = 'DARUNGAN' AND kd_kec = '040';
UPDATE pbjt_assessments SET kd_kel = '005' WHERE kelurahan = 'KEBONSARI' AND kd_kec = '090';

-- TEMPURSARI should be in kd_kec 010, not 070
UPDATE pbjt_assessments SET kd_kec = '010', kd_kel = '005' WHERE kelurahan = 'TEMPURSARI' AND kd_kec = '070';

-- KANDANGTEPUS, SENDURO should be in kd_kec 130 (SENDURO), not 080
UPDATE pbjt_assessments SET kd_kec = '130', kd_kel = '018' WHERE kelurahan = 'KANDANGTEPUS' AND kd_kec = '080';
UPDATE pbjt_assessments SET kd_kec = '130', kd_kel = '012' WHERE kelurahan = 'SENDURO' AND kd_kec = '080';

-- DADAPAN, GUCIALIT should be in kd_kec 140 (GUCIALIT), not 090
UPDATE pbjt_assessments SET kd_kec = '140', kd_kel = '005' WHERE kelurahan = 'DADAPAN' AND kd_kec = '090';
UPDATE pbjt_assessments SET kd_kec = '140', kd_kel = '004' WHERE kelurahan = 'GUCIALIT' AND kd_kec = '090';

-- KLAKAH should be in kd_kec 150, not 100
UPDATE pbjt_assessments SET kd_kec = '150', kd_kel = '001' WHERE kelurahan = 'KLAKAH' AND kd_kec = '100';

-- SUKODONO kelurahan in kd_kec 110 should be 120
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '009' WHERE kelurahan = 'BONDOYUDO' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '013' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'DAWUHANLOR' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '005' WHERE kelurahan = 'KARANGSARI' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '003' WHERE kelurahan = 'KLANTING' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '011' WHERE kelurahan = 'KUTORENON' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '010' WHERE kelurahan = 'SELOKBESUKI' AND kd_kec = '110';
UPDATE pbjt_assessments SET kd_kec = '120', kd_kel = '006' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'SUMBERREJO' AND kd_kec = '110';

-- SUMBERSUKO should be in kd_kec 061
UPDATE pbjt_assessments SET kd_kec = '061', kd_kel = '001' WHERE kelurahan = 'SUMBERSUKO' AND kd_kec = '120';
UPDATE pbjt_assessments SET kd_kec = '061', kd_kel = '002' WHERE kelurahan = 'KEBONSARI' AND kd_kec = '120';

-- JATIROTO should be in kd_kec 100
UPDATE pbjt_assessments SET kd_kec = '100', kd_kel = '006' WHERE kelurahan = 'JATIROTO' AND kd_kec = '130';

-- RANDUAGUNG should be in kd_kec 110
UPDATE pbjt_assessments SET kd_kec = '110', kd_kel = '001' WHERE kelurahan = 'RANDUAGUNG' AND kd_kec = '140';

-- PADANG should be in kd_kec 121
UPDATE pbjt_assessments SET kd_kec = '121', kd_kel = '004' WHERE kelurahan = 'PADANG' AND kd_kec = '150';

-- KEDUNGJAJANG, WONOREJO should be in kd_kec 151
UPDATE pbjt_assessments SET kd_kec = '151', kd_kel = '004' WHERE kelurahan = 'KEDUNGJAJANG' AND kd_kec = '160';
UPDATE pbjt_assessments SET kd_kec = '151', kd_kel = '012' WHERE kelurahan = 'WONOREJO' AND kd_kec = '160';

-- PASIRIAN kelurahan should be in kd_kec 040
UPDATE pbjt_assessments SET kd_kec = '040', kd_kel = '002' WHERE kelurahan = 'BADES' AND kd_kec = '170';
UPDATE pbjt_assessments SET kd_kec = '040', kd_kel = '006' WHERE kelurahan = 'KALIBENDO' AND kd_kec = '170';
UPDATE pbjt_assessments SET kd_kec = '040', kd_kel = '010' WHERE kelurahan = 'NGUTER' AND kd_kec = '170';
UPDATE pbjt_assessments SET kd_kec = '040', kd_kel = '007' WHERE kelurahan = 'PASIRIAN' AND kd_kec = '170';

-- RANUYOSO should be in kd_kec 160
UPDATE pbjt_assessments SET kd_kec = '160', kd_kel = '007' WHERE kelurahan = 'RANUYOSO' AND kd_kec = '180';

-- ROWOKANGKUNG should be in kd_kec 101
UPDATE pbjt_assessments SET kd_kec = '101', kd_kel = '006' WHERE kelurahan = 'ROWOKANGKUNG' AND kd_kec = '190';

-- TEMPEH kelurahan should be in kd_kec 050
UPDATE pbjt_assessments SET kd_kec = '050', kd_kel = '010' WHERE kelurahan = 'JATISARI' AND kd_kec = '200';
UPDATE pbjt_assessments SET kd_kec = '050', kd_kel = '013' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'PANDANARUM' AND kd_kec = '200';
UPDATE pbjt_assessments SET kd_kec = '050', kd_kel = '001' WHERE kelurahan = 'PANDANWANGI' AND kd_kec = '200';
UPDATE pbjt_assessments SET kd_kec = '050', kd_kel = '007' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'TEMPEHLOR' AND kd_kec = '200';
UPDATE pbjt_assessments SET kd_kec = '050', kd_kel = '005' WHERE UPPER(REPLACE(kelurahan, ' ', '')) = 'TEMPEHTENGAH' AND kd_kec = '200';

-- PASRUJAMBE should be in kd_kec 131
UPDATE pbjt_assessments SET kd_kec = '131', kd_kel = '006' WHERE kelurahan = 'PAGOWAN' AND kd_kec = '210';
UPDATE pbjt_assessments SET kd_kec = '131', kd_kel = '001' WHERE kelurahan = 'PASRUJAMBE' AND kd_kec = '210';

-- Fix remaining name variations with spaces
UPDATE pbjt_assessments SET kd_kel = '002' WHERE kd_kec = '030' AND UPPER(REPLACE(kelurahan, ' ', '')) = 'JARIT';
UPDATE pbjt_assessments SET kd_kel = '006' WHERE kd_kec = '020' AND kelurahan = 'PRONOJIWO';
UPDATE pbjt_assessments SET kd_kel = '002' WHERE kd_kec = '020' AND kelurahan = 'TAMANAYU';
UPDATE pbjt_assessments SET kd_kel = '004' WHERE kd_kec = '050' AND kelurahan = 'TEKUNG';
UPDATE pbjt_assessments SET kd_kel = '008' WHERE kd_kec = '050' AND kelurahan = 'TUKUM';
UPDATE pbjt_assessments SET kd_kel = '001' WHERE kd_kec = '050' AND kelurahan = 'WONOGRIYO';
UPDATE pbjt_assessments SET kd_kel = '005' WHERE kd_kec = '050' AND kelurahan = 'WONOKERTO';

-- Additional fixes for kecamatan names that didn't get assigned in first pass
UPDATE pbjt_assessments SET kd_kel = '003' WHERE kd_kec = '030' AND kelurahan = 'CANDIPURO';
UPDATE pbjt_assessments SET kd_kel = '001' WHERE kd_kec = '030' AND kelurahan = 'YUGOSARI';
UPDATE pbjt_assessments SET kd_kel = '007' WHERE kd_kec = '030' AND kelurahan = 'PENANGGAL';
UPDATE pbjt_assessments SET kd_kel = '006' WHERE kd_kec = '030' AND kelurahan = 'CANDIPURO';

UPDATE pbjt_assessments SET kd_kel = '001' WHERE kd_kec = '040' AND UPPER(REPLACE(kelurahan, ' ', '')) IN ('YOSOWILANGUN', 'YOSOWILANGUNKIDUL', 'YOSOWILANGUNLOR');

-- Verify update
SELECT 
    COUNT(*) as total_records,
    COUNT(kd_kel) as records_with_kd_kel,
    COUNT(*) - COUNT(kd_kel) as records_missing_kd_kel
FROM pbjt_assessments;

-- Show summary by kecamatan
SELECT kd_kec, COUNT(*) as total, COUNT(kd_kel) as with_kd_kel
FROM pbjt_assessments 
WHERE kd_kec IS NOT NULL
GROUP BY kd_kec 
ORDER BY kd_kec;
