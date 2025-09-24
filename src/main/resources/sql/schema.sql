-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS sig;

-- Enable UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create bidang table
CREATE TABLE IF NOT EXISTS sig.bidang (
    id uuid DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    kd_prop char(2) NOT NULL,
    kd_dati2 char(2) NOT NULL,
    kd_kec char(3) NOT NULL,
    kd_kel char(3) NOT NULL,
    kd_blok char(3) NOT NULL,
    no_urut char(4) NOT NULL,
    kd_jns_op char NOT NULL,
    nop char(24),
    geom text NOT NULL,
    created_at timestamp DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp,
    updated_by uuid,
    deleted_at timestamp,
    deleted_by uuid,
    recover_at timestamp,
    recover_by uuid,
    is_active boolean DEFAULT true NOT NULL,
    CONSTRAINT nop_ukey UNIQUE (kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op)
);

-- Set table owner
ALTER TABLE sig.bidang OWNER TO postgres;

-- Create unique index
CREATE UNIQUE INDEX IF NOT EXISTS bidang_nop_idx ON sig.bidang (nop);

-- Insert sample data (the data you provided)
INSERT INTO sig.bidang (
    id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, nop, geom, 
    created_at, created_by, updated_at, updated_by, is_active
) VALUES (
    '92523f4a-7722-4acd-ac63-5085f964dcd0'::uuid,
    '35',
    '08',
    '130',
    '017',
    '005',
    '0139',
    '0',
    '35.08.130.017.005.0139.0',
    '01030000A0E610000001000000050000001EB27FCF2B465C405019E756492420C000000000000000002101629D2A465C40C3E02E5F622420C00000000000000000A964AE8025465C4062DC943B572420C00000000000000000D96C1FF626465C403F7FCD5F3F2420C000000000000000001EB27FCF2B465C405019E756492420C00000000000000000',
    '2024-03-18 03:26:23.148012'::timestamp,
    '030b6bee-dd8a-435a-b0af-2162115ac549'::uuid,
    '2024-03-18 03:26:23.148012'::timestamp,
    '030b6bee-dd8a-435a-b0af-2162115ac549'::uuid,
    true
);

