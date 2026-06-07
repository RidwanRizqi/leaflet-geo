-- =====================================================
-- ROLE-MENU MAPPING SETUP
-- Digunakan untuk mapping role ke menu (RBAC Menu)
-- =====================================================

-- Create table for role-menu mappings
CREATE TABLE IF NOT EXISTS system.role_menu_mapping (
    id          VARCHAR(36) PRIMARY KEY,
    role_name   VARCHAR(25) NOT NULL,
    menu_id     INTEGER NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups by role
CREATE INDEX IF NOT EXISTS idx_role_menu_role_name ON system.role_menu_mapping(role_name);
CREATE INDEX IF NOT EXISTS idx_role_menu_menu_id ON system.role_menu_mapping(menu_id);

-- Seed default: ADMIN role gets access to ALL existing menus
-- Menu IDs from frontend menu.ts:
-- 1 (Title), 7 (Dashboard Pajak), 8 (Dashboard Pendapatan),
-- 11 (PBJT Assessment), 112 (PBJT Mamin), 1121 (Daftar Asesmen Mamin), 1122 (Peta Mamin),
-- 113 (PBJT Hotel), 11301 (Daftar Asesmen Hotel), 11302 (Peta Hotel),
-- 1131 (Dashboard Hotel), 1132 (Properties), 1133 (Projections),
-- 1134 (Formalization), 1135 (Reports),
-- 6 (Bidang), 61 (Map Bidang), 62 (List Bidang),
-- 10 (Tematik), 101-108 (sub-tematik),
-- 9 (Setting), 91 (Kecamatan), 92 (Kelurahan), 93 (Blok), 94 (Anggaran)

INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'ADMIN', m.id, NOW(), NOW()
FROM (
    VALUES (1), (7), (8), (11), (112), (1121), (1122),
           (113), (11301), (11302), (1131), (1132), (1133), (1134), (1135),
           (6), (61), (62),
           (10), (101), (102), (103), (104), (105), (106), (107), (108),
           (9), (91), (92), (93), (94)
) AS m(id)
WHERE NOT EXISTS (
    SELECT 1 FROM system.role_menu_mapping WHERE role_name = 'ADMIN' AND menu_id = m.id
);
