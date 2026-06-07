-- SEED ROLES + USERS (production ready)
-- ==============================================

-- 1. SEED ROLE-MENU MAPPINGS
DELETE FROM system.role_menu_mapping WHERE role_name IN
('ADMIN','EKSEKUTIF','GIS','PBJT-HOTEL','PBJT-MAMIN');

-- ADMIN = all 32 menus
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'ADMIN', m.id, NOW(), NOW()
FROM (VALUES (1),(6),(61),(62),(7),(8),(9),(91),(92),(93),(94),(95),(10),(101),(102),(103),(104),(105),(106),(107),(108),(11),(112),(1121),(1122),(113),(11301),(11302),(1131),(1132),(1133),(1134),(1135)) AS m(id);

-- EKSEKUTIF = dashboards only
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'EKSEKUTIF', m.id, NOW(), NOW()
FROM (VALUES (1),(7),(8)) AS m(id);

-- GIS = bidang maps + tematik maps
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'GIS', m.id, NOW(), NOW()
FROM (VALUES (1),(6),(61),(62),(10),(101),(102),(103),(104),(105),(106),(107),(108)) AS m(id);

-- PBJT-HOTEL = PBJT Hotel assessment
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'PBJT-HOTEL', m.id, NOW(), NOW()
FROM (VALUES (1),(11),(113),(11301),(11302),(1131),(1132),(1133),(1134),(1135)) AS m(id);

-- PBJT-MAMIN = PBJT Mamin assessment
INSERT INTO system.role_menu_mapping (id, role_name, menu_id, created_at, updated_at)
SELECT gen_random_uuid()::VARCHAR, 'PBJT-MAMIN', m.id, NOW(), NOW()
FROM (VALUES (1),(11),(112),(1121),(1122)) AS m(id);

-- 2. CREATE USERS (BCrypt hashes)
DELETE FROM system.user WHERE username IN ('admin','eksekutif','gis','pbjthotel','pbjtmamin');
INSERT INTO system.user (username, password, nama, role, is_admin, is_active, created_at, updated_at) VALUES ('admin', '$2a$10$W6FTOwaJq8jqK4LKLGqga.dJN2Go3FKboNlthD7JvykeOE7r99Ewm', 'Administrator', 'ADMIN', true, true, NOW(), NOW());
INSERT INTO system.user (username, password, nama, role, is_admin, is_active, created_at, updated_at) VALUES ('eksekutif', '$2a$10$U67QtY3zCqibm9IFctc0zesVcuBTogJ.rDAN4tzAF.RMJ5yuGAcUO', 'Eksekutif', 'EKSEKUTIF', false, true, NOW(), NOW());
INSERT INTO system.user (username, password, nama, role, is_admin, is_active, created_at, updated_at) VALUES ('gis', '$2a$10$zYoyAIm5gnejJXK2HMX7rejkcyJC/W0dfSGvFK5q67UGDzRT5fm0y', 'GIS Operator', 'GIS', false, true, NOW(), NOW());
INSERT INTO system.user (username, password, nama, role, is_admin, is_active, created_at, updated_at) VALUES ('pbjthotel', '$2a$10$Bv6pp2vMQrNy5cP6XuUmkujSkJ5eq.ydGZHmnM5L2iZSp.FJkyISy', 'Petugas PBJT Hotel', 'PBJT-HOTEL', false, true, NOW(), NOW());
INSERT INTO system.user (username, password, nama, role, is_admin, is_active, created_at, updated_at) VALUES ('pbjtmamin', '$2a$10$fpmByrsmDHf9Mcs./RV3oepyqmCAveLfn8aEeeOMaEuv2BNdXARZK', 'Petugas PBJT Mamin', 'PBJT-MAMIN', false, true, NOW(), NOW());

-- DONE
