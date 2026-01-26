-- Add kd_kec column to pbjt_assessments table
ALTER TABLE pbjt_assessments 
ADD COLUMN IF NOT EXISTS kd_kec VARCHAR(10);

-- Add comment for documentation
COMMENT ON COLUMN pbjt_assessments.kd_kec IS 'Kode kecamatan untuk drill-down ke kelurahan di map';

-- Verify the column was added
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'pbjt_assessments' AND column_name = 'kd_kec';
