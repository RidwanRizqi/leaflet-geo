-- Migration to update sample_transactions structure to include notes for each transaction
-- Changes sample_transactions from array of decimals to array of objects {amount: decimal, notes: text}

-- Note: Since PostgreSQL JSONB arrays can't be directly migrated from numeric[] to jsonb[],
-- we need to transform existing data or start fresh.
-- For existing data, we'll convert numeric values to objects with amount field and empty notes.

-- Add a temporary column to store the new structure
ALTER TABLE pbjt_observation_history 
ADD COLUMN IF NOT EXISTS sample_transactions_new jsonb;

-- Migrate existing data: Convert numeric array to jsonb array of objects
-- Format: [10000, 20000] becomes [{"amount": 10000, "notes": ""}, {"amount": 20000, "notes": ""}]
UPDATE pbjt_observation_history
SET sample_transactions_new = (
    SELECT jsonb_agg(
        jsonb_build_object(
            'amount', value::numeric,
            'notes', ''
        )
    )
    FROM jsonb_array_elements_text(to_jsonb(sample_transactions)) AS value
)
WHERE sample_transactions IS NOT NULL;

-- Drop old column
ALTER TABLE pbjt_observation_history DROP COLUMN sample_transactions;

-- Rename new column to original name
ALTER TABLE pbjt_observation_history 
RENAME COLUMN sample_transactions_new TO sample_transactions;

-- Add comment to document the structure
COMMENT ON COLUMN pbjt_observation_history.sample_transactions IS 
'Array of sample transaction objects with structure: [{"amount": number, "notes": "text"}]';
