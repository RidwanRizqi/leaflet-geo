package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/bidang")
@CrossOrigin(origins = "*")
public class BidangController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            Long count = postgresJdbcTemplate.queryForObject("SELECT COUNT(*) FROM sig.bidang", Long.class);
            return ResponseEntity.ok("Database connected successfully! Total records: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Legacy-compatible list endpoint
     * URL Pattern: GET /api/bidang/list?kd_kec=130&kd_kel=017&kd_blok=005
     * Returns all active bidang for the given kecamatan, kelurahan, and blok
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getBidangList(
            @RequestParam String kd_kec,
            @RequestParam String kd_kel,
            @RequestParam String kd_blok) {
        try {
            // Query bidang with WKB geometry (same format as legacy)
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, nop, " +
                            "encode(ST_AsBinary(geom), 'hex') as geom, created_at, is_active " +
                            "FROM sig.bidang WHERE kd_kec = ? AND kd_kel = ? AND kd_blok = ? AND is_active = true " +
                            "ORDER BY no_urut",
                    kd_kec, kd_kel, kd_blok);

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBidang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE is_active = true", Long.class);

            // Get paginated data
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBidangById(@PathVariable String id) {
        try {
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE id = ?", id);
            if (data.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(data.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/nop/{nop}")
    public ResponseEntity<Map<String, Object>> getBidangByNop(@PathVariable String nop) {
        try {
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE nop = ?", nop);
            if (data.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(data.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/geometry")
    public ResponseEntity<Map<String, Object>> getAllBidangWithGeometry(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE is_active = true AND geom IS NOT NULL", Long.class);

            // Get paginated data with GeoJSON conversion
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, nop, ST_AsGeoJSON(geom) as geojson, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, created_at, is_active FROM sig.bidang WHERE is_active = true AND geom IS NOT NULL ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/province/{kdProp}")
    public ResponseEntity<List<Map<String, Object>>> getBidangByProvince(@PathVariable String kdProp) {
        try {
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE kd_prop = ? AND is_active = true", kdProp);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/kecamatan/{kdProp}/{kdDati2}/{kdKec}")
    public ResponseEntity<Map<String, Object>> getBidangByKecamatan(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true",
                    Long.class, kdProp, kdDati2, kdKec);

            // Get paginated data
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    kdProp, kdDati2, kdKec, size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "filters", Map.of(
                            "kdProp", kdProp,
                            "kdDati2", kdDati2,
                            "kdKec", kdKec),
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/kelurahan/{kdProp}/{kdDati2}/{kdKec}/{kdKel}")
    public ResponseEntity<Map<String, Object>> getBidangByKelurahan(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec,
            @PathVariable String kdKel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND kd_kel = ? AND is_active = true",
                    Long.class, kdProp, kdDati2, kdKec, kdKel);

            // Get paginated data
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT * FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND kd_kel = ? AND is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    kdProp, kdDati2, kdKec, kdKel, size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "filters", Map.of(
                            "kdProp", kdProp,
                            "kdDati2", kdDati2,
                            "kdKec", kdKec,
                            "kdKel", kdKel),
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/kecamatan/{kdProp}/{kdDati2}/{kdKec}/geometry")
    public ResponseEntity<Map<String, Object>> getBidangByKecamatanWithGeometry(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true AND geom IS NOT NULL",
                    Long.class, kdProp, kdDati2, kdKec);

            // Get paginated data with GeoJSON conversion
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, nop, ST_AsGeoJSON(geom) as geojson, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, created_at, is_active FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true AND geom IS NOT NULL ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    kdProp, kdDati2, kdKec, size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "filters", Map.of(
                            "kdProp", kdProp,
                            "kdDati2", kdDati2,
                            "kdKec", kdKec),
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/kelurahan/{kdProp}/{kdDati2}/{kdKec}/{kdKel}/geometry")
    public ResponseEntity<Map<String, Object>> getBidangByKelurahanWithGeometry(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec,
            @PathVariable String kdKel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            int offset = page * size;

            // Get total count
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND kd_kel = ? AND is_active = true AND geom IS NOT NULL",
                    Long.class, kdProp, kdDati2, kdKec, kdKel);

            // Get paginated data with GeoJSON conversion
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, nop, ST_AsGeoJSON(geom) as geojson, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, created_at, is_active FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND kd_kel = ? AND is_active = true AND geom IS NOT NULL ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    kdProp, kdDati2, kdKec, kdKel, size, offset);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "filters", Map.of(
                            "kdProp", kdProp,
                            "kdDati2", kdDati2,
                            "kdKec", kdKec,
                            "kdKel", kdKel),
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBidang(
            @RequestParam(required = false) String kdProp,
            @RequestParam(required = false) String kdDati2,
            @RequestParam(required = false) String kdKec,
            @RequestParam(required = false) String kdKel,
            @RequestParam(required = false) String nop,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;

            // Build dynamic query
            StringBuilder whereClause = new StringBuilder("WHERE is_active = true");
            java.util.List<Object> params = new java.util.ArrayList<>();

            if (kdProp != null && !kdProp.isEmpty()) {
                whereClause.append(" AND kd_prop = ?");
                params.add(kdProp);
            }
            if (kdDati2 != null && !kdDati2.isEmpty()) {
                whereClause.append(" AND kd_dati2 = ?");
                params.add(kdDati2);
            }
            if (kdKec != null && !kdKec.isEmpty()) {
                whereClause.append(" AND kd_kec = ?");
                params.add(kdKec);
            }
            if (kdKel != null && !kdKel.isEmpty()) {
                whereClause.append(" AND kd_kel = ?");
                params.add(kdKel);
            }
            if (nop != null && !nop.isEmpty()) {
                whereClause.append(" AND nop ILIKE ?");
                params.add("%" + nop + "%");
            }

            // Get total count
            String countQuery = "SELECT COUNT(*) FROM sig.bidang " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countQuery, Long.class, params.toArray());

            // Get paginated data
            String dataQuery = "SELECT * FROM sig.bidang " + whereClause + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
            params.add(size);
            params.add(offset);

            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(dataQuery, params.toArray());

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = Map.of(
                    "data", data,
                    "filters", Map.of(
                            "kdProp", kdProp != null ? kdProp : "",
                            "kdDati2", kdDati2 != null ? kdDati2 : "",
                            "kdKec", kdKec != null ? kdKec : "",
                            "kdKel", kdKel != null ? kdKel : "",
                            "nop", nop != null ? nop : ""),
                    "pagination", Map.of(
                            "page", page,
                            "size", size,
                            "totalElements", totalCount,
                            "totalPages", totalPages,
                            "hasNext", hasNext,
                            "hasPrev", hasPrev));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/setup")
    public ResponseEntity<Map<String, Object>> setupDatabase() {
        try {
            // Create schema if not exists
            postgresJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS sig");

            // Create table if not exists
            postgresJdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS sig.bidang (
                            id uuid DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
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
                        )
                    """);

            // Insert sample data
            postgresJdbcTemplate.execute(
                    """
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
                                ) ON CONFLICT (id) DO NOTHING
                            """);

            // Test the setup
            Long count = postgresJdbcTemplate.queryForObject("SELECT COUNT(*) FROM sig.bidang", Long.class);

            return ResponseEntity.ok(Map.of(
                    "status", "Database setup completed successfully",
                    "recordCount", count,
                    "message", "Schema sig and table bidang created with sample data"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "status", "Setup failed"));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        try {
            // Test 1: Basic connection
            String connectionTest = postgresJdbcTemplate.queryForObject("SELECT 'Database connected' as status",
                    String.class);

            // Test 2: List all schemas
            List<Map<String, Object>> schemas = postgresJdbcTemplate.queryForList(
                    "SELECT schema_name FROM information_schema.schemata ORDER BY schema_name");

            // Test 3: Check if sig schema exists specifically
            String sigExists = postgresJdbcTemplate.queryForObject(
                    "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'sig') THEN 'sig schema exists' ELSE 'sig schema does not exist' END",
                    String.class);

            // Test 4: List all tables with 'bidang' in name
            List<Map<String, Object>> tables = postgresJdbcTemplate.queryForList(
                    "SELECT table_schema, table_name FROM information_schema.tables WHERE table_name LIKE '%bidang%'");

            // Test 4: Try different table name formats
            String tableQuery = "";
            List<Map<String, Object>> data = List.of();

            try {
                // Try with public schema first
                data = postgresJdbcTemplate.queryForList("SELECT * FROM public.bidang LIMIT 1");
                tableQuery = "public.bidang - SUCCESS";
            } catch (Exception e1) {
                try {
                    // Try without schema
                    data = postgresJdbcTemplate.queryForList("SELECT * FROM bidang LIMIT 1");
                    tableQuery = "bidang (no schema) - SUCCESS";
                } catch (Exception e2) {
                    try {
                        // Try with quotes
                        data = postgresJdbcTemplate.queryForList("SELECT * FROM \"public\".\"bidang\" LIMIT 1");
                        tableQuery = "public.bidang (with quotes) - SUCCESS";
                    } catch (Exception e3) {
                        try {
                            // Try sig schema
                            data = postgresJdbcTemplate.queryForList("SELECT * FROM sig.bidang LIMIT 1");
                            tableQuery = "sig.bidang - SUCCESS";
                        } catch (Exception e4) {
                            tableQuery = "All attempts failed. Last error: " + e4.getMessage();
                        }
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "connection", connectionTest,
                    "schemas", schemas,
                    "sigExists", sigExists,
                    "tables", tables,
                    "tableQuery", tableQuery,
                    "dataCount", data.size(),
                    "sampleData", data.isEmpty() ? "No data" : data.get(0)));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "connection", "Failed"));
        }
    }

    /**
     * Get kecamatan with count from PostgreSQL
     * Uses system.kecamatan for names and sig.bidang for counts
     */
    @GetMapping("/kecamatan-with-count/{kdProp}/{kdDati2}")
    public ResponseEntity<Map<String, Object>> getKecamatanWithCount(
            @PathVariable String kdProp,
            @PathVariable String kdDati2) {
        try {
            // Step 1: Get count from sig.bidang
            List<Map<String, Object>> countData = postgresJdbcTemplate.queryForList(
                    "SELECT kd_kec, COUNT(*) as jumlah_bidang FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND is_active = true GROUP BY kd_kec ORDER BY kd_kec",
                    kdProp, kdDati2);

            // Create count map
            Map<String, Integer> countMap = new HashMap<>();
            for (Map<String, Object> row : countData) {
                String kdKec = (String) row.get("kd_kec");
                Integer count = ((Number) row.get("jumlah_bidang")).intValue();
                countMap.put(kdKec.trim(), count);
            }

            // Step 2: Get names from PostgreSQL system.kecamatan
            List<Map<String, Object>> kecamatanData = postgresJdbcTemplate.queryForList(
                    "SELECT kd_prop, kd_dati2, kd_kec, nama FROM system.kecamatan WHERE kd_prop = ? AND kd_dati2 = ? AND is_active = true ORDER BY kd_kec",
                    kdProp, kdDati2);

            // Step 3: Combine data
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> kecamatan : kecamatanData) {
                String kdKec = ((String) kecamatan.get("kd_kec")).trim();
                Integer count = countMap.getOrDefault(kdKec, 0);

                Map<String, Object> combined = new HashMap<>();
                combined.put("kdPropinsi", ((String) kecamatan.get("kd_prop")).trim());
                combined.put("kdDati2", ((String) kecamatan.get("kd_dati2")).trim());
                combined.put("kdKecamatan", kdKec);
                combined.put("nmKecamatan", kecamatan.get("nama"));
                combined.put("jumlahBidang", count);

                result.add(combined);
            }

            return ResponseEntity.ok(Map.of(
                    "data", result,
                    "success", true,
                    "message", "Kecamatan with count retrieved successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "success", false));
        }
    }

    /**
     * Get kelurahan with count from PostgreSQL
     * Uses system.kelurahan for names and sig.bidang for counts
     */
    @GetMapping("/kelurahan-with-count/{kdProp}/{kdDati2}/{kdKec}")
    public ResponseEntity<Map<String, Object>> getKelurahanWithCount(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec) {
        try {
            // Step 1: Get count from sig.bidang
            List<Map<String, Object>> countData = postgresJdbcTemplate.queryForList(
                    "SELECT kd_kel, COUNT(*) as jumlah_bidang FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true GROUP BY kd_kel ORDER BY kd_kel",
                    kdProp, kdDati2, kdKec);

            // Create count map
            Map<String, Integer> countMap = new HashMap<>();
            for (Map<String, Object> row : countData) {
                String kdKel = (String) row.get("kd_kel");
                Integer count = ((Number) row.get("jumlah_bidang")).intValue();
                countMap.put(kdKel.trim(), count);
            }

            // Step 2: Get names from PostgreSQL system.kelurahan
            List<Map<String, Object>> kelurahanData = postgresJdbcTemplate.queryForList(
                    "SELECT kd_prop, kd_dati2, kd_kec, kd_kel, nama FROM system.kelurahan WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND is_active = true ORDER BY kd_kel",
                    kdProp, kdDati2, kdKec);

            // Step 3: Combine data
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> kelurahan : kelurahanData) {
                String kdKel = ((String) kelurahan.get("kd_kel")).trim();
                Integer count = countMap.getOrDefault(kdKel, 0);

                Map<String, Object> combined = new HashMap<>();
                combined.put("kdPropinsi", ((String) kelurahan.get("kd_prop")).trim());
                combined.put("kdDati2", ((String) kelurahan.get("kd_dati2")).trim());
                combined.put("kdKecamatan", ((String) kelurahan.get("kd_kec")).trim());
                combined.put("kdKelurahan", kdKel);
                combined.put("nmKelurahan", kelurahan.get("nama"));
                combined.put("jumlahBidang", count);

                result.add(combined);
            }

            return ResponseEntity.ok(Map.of(
                    "data", result,
                    "success", true,
                    "message", "Kelurahan with count retrieved successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "success", false));
        }
    }

    /**
     * Get total count of all bidang
     */
    @GetMapping("/total-count")
    public ResponseEntity<Map<String, Object>> getTotalBidangCount() {
        try {
            Long totalCount = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE is_active = true", Long.class);

            return ResponseEntity.ok(Map.of(
                    "totalBidang", totalCount,
                    "success", true,
                    "message", "Total bidang count retrieved successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "success", false));
        }
    }

    /**
     * Get blok with count from PostgreSQL
     */
    @GetMapping("/blok-with-count/{kdProp}/{kdDati2}/{kdKec}/{kdKel}")
    public ResponseEntity<Map<String, Object>> getBlokWithCount(
            @PathVariable String kdProp,
            @PathVariable String kdDati2,
            @PathVariable String kdKec,
            @PathVariable String kdKel) {
        try {
            // Step 1: Get count from PostgreSQL
            List<Map<String, Object>> countData = postgresJdbcTemplate.queryForList(
                    "SELECT kd_blok, COUNT(*) as jumlah_bidang FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND kd_kel = ? AND is_active = true GROUP BY kd_blok ORDER BY kd_blok",
                    kdProp, kdDati2, kdKec, kdKel);

            // Step 2: Create result list with blok codes and counts
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> row : countData) {
                Map<String, Object> blokData = new HashMap<>();
                blokData.put("kdBlok", row.get("kd_blok"));
                blokData.put("nmBlok", "Blok " + row.get("kd_blok")); // Simple name generation
                blokData.put("jumlahBidang", ((Long) row.get("jumlah_bidang")).intValue());
                result.add(blokData);
            }

            return ResponseEntity.ok(Map.of(
                    "data", result,
                    "success", true,
                    "message", "Blok with count retrieved successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "success", false));
        }
    }
}
