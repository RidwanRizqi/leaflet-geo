-- Add kd_kel column to pbjt_assessments table
ALTER TABLE pbjt_assessments ADD COLUMN IF NOT EXISTS kd_kel VARCHAR(10);

-- Verify column was added
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'pbjt_assessments' 
AND column_name IN ('kd_kec', 'kd_kel');
