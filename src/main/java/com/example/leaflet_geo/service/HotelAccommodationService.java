package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for hotel/accommodation (PBJT Jasa Perhotelan) data.
 *
 * Architecture:
 * - READ/WRITE (CRUD): operates on LOCAL PostgreSQL (pbjt_assessment_db)
 * - SYNC: imports data FROM SIMATDA MySQL (read-only, never modifies SIMATDA)
 *
 * In SIMATDA: t_jenisobjek = 1 = "Pajak Hotel" (PBJT Jasa Perhotelan)
 */
@Service
@Slf4j
public class HotelAccommodationService {

    private final JdbcTemplate mysqlJdbcTemplate;   // SIMATDA (read-only)
    private final JdbcTemplate pbjtJdbcTemplate;     // Local PostgreSQL (CRUD)

    private static final int JENIS_HOTEL = 1;

    public HotelAccommodationService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("pbjtJdbcTemplate") JdbcTemplate pbjtJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.pbjtJdbcTemplate = pbjtJdbcTemplate;
    }

    // ==================== LOCAL CRUD OPERATIONS ====================

    /**
     * Get all hotels from LOCAL database
     */
    public List<Map<String, Object>> getAllHotels() {
        String query = """
                SELECT * FROM hotel_accommodations
                ORDER BY property_name
                """;
        try {
            return cleanRowMaps(pbjtJdbcTemplate.queryForList(query));
        } catch (Exception e) {
            log.error("Error fetching hotels from local DB: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get hotel by ID from LOCAL database
     */
    public Map<String, Object> getHotelById(long id) {
        String query = "SELECT * FROM hotel_accommodations WHERE id = ?";
        try {
            List<Map<String, Object>> results = pbjtJdbcTemplate.queryForList(query, id);
            return results.isEmpty() ? null : cleanRowMap(results.get(0));
        } catch (Exception e) {
            log.error("Error fetching hotel {}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * Create hotel in LOCAL database
     */
    public Map<String, Object> createHotel(Map<String, Object> data) {
        String query = """
                INSERT INTO hotel_accommodations
                (accommodation_type, property_name, owner_name, owner_phone, address,
                 kelurahan, kecamatan, kabupaten, latitude, longitude, total_rooms,
                 building_area, land_area, has_business_permit, has_tax_registration,
                 formalization_status, estimated_annual_revenue, projected_annual_tax,
                 tax_rate, willing_to_formalize, status, npwpd, tax_object_id,
                 photo_urls, supporting_doc_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING *
                """;
        try {
            return cleanRowMap(pbjtJdbcTemplate.queryForMap(query,
                    getStr(data, "accommodation_type", "HOTEL"),
                    getStr(data, "property_name", ""),
                    getStr(data, "owner_name", null),
                    getStr(data, "owner_phone", null),
                    getStr(data, "address", null),
                    getStr(data, "kelurahan", null),
                    getStr(data, "kecamatan", null),
                    getStr(data, "kabupaten", "KABUPATEN LUMAJANG"),
                    getNum(data, "latitude"),
                    getNum(data, "longitude"),
                    getInt(data, "total_rooms"),
                    getNum(data, "building_area"),
                    getNum(data, "land_area"),
                    getBool(data, "has_business_permit", false),
                    getBool(data, "has_tax_registration", false),
                    getStr(data, "formalization_status", "INFORMAL"),
                    getNum(data, "estimated_annual_revenue"),
                    getNum(data, "projected_annual_tax"),
                    getNum(data, "tax_rate"),
                    getBool(data, "willing_to_formalize", null),
                    getStr(data, "status", "ACTIVE"),
                    getStr(data, "npwpd", null),
                    getStr(data, "tax_object_id", null),
                    getPhotoUrls(data),
                    getStr(data, "supporting_doc_url", null)));
        } catch (Exception e) {
            log.error("Error creating hotel: {}", e.getMessage());
            throw new RuntimeException("Failed to create hotel: " + e.getMessage());
        }
    }

    /**
     * Update hotel in LOCAL database
     */
    public Map<String, Object> updateHotel(long id, Map<String, Object> data) {
        String query = """
                UPDATE hotel_accommodations SET
                    accommodation_type = COALESCE(?, accommodation_type),
                    property_name = COALESCE(?, property_name),
                    owner_name = COALESCE(?, owner_name),
                    owner_phone = COALESCE(?, owner_phone),
                    address = COALESCE(?, address),
                    kelurahan = COALESCE(?, kelurahan),
                    kecamatan = COALESCE(?, kecamatan),
                    latitude = COALESCE(?, latitude),
                    longitude = COALESCE(?, longitude),
                    total_rooms = COALESCE(?, total_rooms),
                    has_business_permit = COALESCE(?, has_business_permit),
                    has_tax_registration = COALESCE(?, has_tax_registration),
                    formalization_status = COALESCE(?, formalization_status),
                    estimated_annual_revenue = COALESCE(?, estimated_annual_revenue),
                    projected_annual_tax = COALESCE(?, projected_annual_tax),
                    status = COALESCE(?, status),
                    photo_urls = ?,
                    supporting_doc_url = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                RETURNING *
                """;
        try {
            return cleanRowMap(pbjtJdbcTemplate.queryForMap(query,
                    getStr(data, "accommodation_type", null),
                    getStr(data, "property_name", null),
                    getStr(data, "owner_name", null),
                    getStr(data, "owner_phone", null),
                    getStr(data, "address", null),
                    getStr(data, "kelurahan", null),
                    getStr(data, "kecamatan", null),
                    getNum(data, "latitude"),
                    getNum(data, "longitude"),
                    getInt(data, "total_rooms"),
                    getBool(data, "has_business_permit", null),
                    getBool(data, "has_tax_registration", null),
                    getStr(data, "formalization_status", null),
                    getNum(data, "estimated_annual_revenue"),
                    getNum(data, "projected_annual_tax"),
                    getStr(data, "status", null),
                    getPhotoUrls(data),
                    getStr(data, "supporting_doc_url", null),
                    id));
        } catch (Exception e) {
            log.error("Error updating hotel {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update hotel: " + e.getMessage());
        }
    }

    /**
     * Delete hotel from LOCAL database
     */
    public void deleteHotel(long id) {
        try {
            // Delete related realisasi first
            pbjtJdbcTemplate.update("DELETE FROM hotel_realisasi WHERE hotel_id = ?", id);
            int deleted = pbjtJdbcTemplate.update("DELETE FROM hotel_accommodations WHERE id = ?", id);
            if (deleted == 0) {
                throw new RuntimeException("Hotel not found with ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error deleting hotel {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete hotel: " + e.getMessage());
        }
    }

    // ==================== DASHBOARD & ANALYTICS (LOCAL) ====================

    /**
     * Get dashboard metrics from LOCAL database
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        try {
            // Total properties
            Integer total = pbjtJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hotel_accommodations", Integer.class);
            metrics.put("totalProperties", total != null ? total : 0);

            // Active vs closed
            Integer active = pbjtJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hotel_accommodations WHERE status = 'ACTIVE'", Integer.class);
            Integer closed = pbjtJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM hotel_accommodations WHERE status != 'ACTIVE' OR is_closed = true", Integer.class);
            metrics.put("activeProperties", active != null ? active : 0);
            metrics.put("closedProperties", closed != null ? closed : 0);

            // By accommodation type
            List<Map<String, Object>> byType = pbjtJdbcTemplate.queryForList(
                    "SELECT accommodation_type, COUNT(*) as count FROM hotel_accommodations GROUP BY accommodation_type ORDER BY count DESC");
            metrics.put("byType", byType);

            // Revenue totals
            Map<String, Object> revenueTotals = pbjtJdbcTemplate.queryForMap(
                    "SELECT COALESCE(SUM(estimated_annual_revenue), 0) as total_revenue, COALESCE(SUM(projected_annual_tax), 0) as total_tax FROM hotel_accommodations WHERE status = 'ACTIVE'");
            metrics.put("totalRevenue", revenueTotals.get("total_revenue"));
            metrics.put("totalTax", revenueTotals.get("total_tax"));

            // Formalization stats
            List<Map<String, Object>> formalization = pbjtJdbcTemplate.queryForList(
                    "SELECT formalization_status, COUNT(*) as count FROM hotel_accommodations GROUP BY formalization_status");
            metrics.put("formalization", formalization);

            // By kecamatan
            List<Map<String, Object>> byKecamatan = pbjtJdbcTemplate.queryForList(
                    "SELECT kecamatan, COUNT(*) as count FROM hotel_accommodations WHERE kecamatan IS NOT NULL GROUP BY kecamatan ORDER BY count DESC");
            metrics.put("byKecamatan", byKecamatan);

            // Yearly realization from local hotel_realisasi
            List<Map<String, Object>> yearlyStats = pbjtJdbcTemplate.queryForList("""
                    SELECT tahun as year,
                           SUM(total_revenue) as total_revenue,
                           SUM(total_tax) as total_tax,
                           SUM(total_payment) as total_payment,
                           SUM(transaction_count) as transaction_count
                    FROM hotel_realisasi
                    GROUP BY tahun
                    ORDER BY tahun
                    """);
            metrics.put("yearlyStats", yearlyStats);

        } catch (Exception e) {
            log.error("Error generating dashboard metrics: {}", e.getMessage());
            metrics.put("error", e.getMessage());
        }

        return metrics;
    }

    /**
     * Get all hotels with their realization data from LOCAL database
     */
    public List<Map<String, Object>> getAllHotelsWithRealization() {
        String query = """
                       SELECT h.*,
                       COALESCE(r2022.total_tax, 0) as realisasi_2022,
                       COALESCE(r2023.total_tax, 0) as realisasi_2023,
                       COALESCE(r2024.total_tax, 0) as realisasi_2024,
                       COALESCE(r2025.total_tax, 0) as realisasi_2025,
                       COALESCE(r2026.total_tax, 0) as realisasi_2026,
                       COALESCE(r2022.total_tax, 0) + COALESCE(r2023.total_tax, 0) +
                       COALESCE(r2024.total_tax, 0) + COALESCE(r2025.total_tax, 0) +
                       COALESCE(r2026.total_tax, 0) as total_realisasi
                FROM hotel_accommodations h
                LEFT JOIN hotel_realisasi r2022 ON h.id = r2022.hotel_id AND r2022.tahun = '2022'
                LEFT JOIN hotel_realisasi r2023 ON h.id = r2023.hotel_id AND r2023.tahun = '2023'
                LEFT JOIN hotel_realisasi r2024 ON h.id = r2024.hotel_id AND r2024.tahun = '2024'
                LEFT JOIN hotel_realisasi r2025 ON h.id = r2025.hotel_id AND r2025.tahun = '2025'
                LEFT JOIN hotel_realisasi r2026 ON h.id = r2026.hotel_id AND r2026.tahun = '2026'
                ORDER BY total_realisasi DESC
                """;
        try {
            return cleanRowMaps(pbjtJdbcTemplate.queryForList(query));
        } catch (Exception e) {
            log.error("Error fetching hotels with realization: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get realization for a specific hotel from LOCAL database
     */
    public List<Map<String, Object>> getHotelRealization(long hotelId) {
        String query = """
                SELECT tahun as year, total_revenue, total_tax, total_payment, transaction_count
                FROM hotel_realisasi
                WHERE hotel_id = ?
                ORDER BY tahun
                """;
        try {
            return pbjtJdbcTemplate.queryForList(query, hotelId);
        } catch (Exception e) {
            log.error("Error fetching realization for hotel {}: {}", hotelId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== SYNC FROM SIMATDA (READ-ONLY) ====================

    /**
     * Sync hotel objects from SIMATDA MySQL → local PostgreSQL.
     * SIMATDA is READ-ONLY — this only reads from SIMATDA and writes to local.
     */
    public Map<String, Object> syncFromSimatda() {
        log.info("=== Starting hotel sync from SIMATDA ===");
        Map<String, Object> result = new LinkedHashMap<>();
        int synced = 0;
        int updated = 0;
        int errors = 0;

        try {
            // 1. Fetch all hotel objects from SIMATDA (READ ONLY)
            List<Map<String, Object>> simatdaHotels = mysqlJdbcTemplate.queryForList("""
                    SELECT o.t_idobjek as simatda_id,
                           o.t_namaobjek as property_name,
                           o.t_alamatobjek as address,
                           o.t_latitudeobjek as latitude,
                           o.t_longitudeobjek as longitude,
                           o.t_objektutup as is_closed,
                           o.t_notelpobjek as phone,
                           o.t_tgldaftarobjek as registration_date,
                           w.t_idwp as wp_id,
                           w.t_nama as wp_name,
                           w.t_namapemilik as owner_name,
                           w.t_nohp as owner_phone,
                           COALESCE(c.t_npwpdwp, '') as npwpd,
                           COALESCE(c.t_nop, '') as object_number,
                           kec.s_namakec as kecamatan,
                           kel.s_namakel as kelurahan
                    FROM t_wpobjek o
                    LEFT JOIN t_wp w ON o.t_idwp = w.t_idwp
                    LEFT JOIN cetak_daftar_wp_per_jenis_pajak c ON o.t_namaobjek = c.t_namaobjek AND o.t_jenisobjek = c.t_jenisobjek
                    LEFT JOIN s_kecamatan kec ON o.t_kecamatanobjek = kec.s_idkec
                    LEFT JOIN s_kelurahan kel ON o.t_kelurahanobjek = kel.s_idkel
                    WHERE o.t_jenisobjek = ?
                    """, JENIS_HOTEL);

            log.info("Fetched {} hotel objects from SIMATDA", simatdaHotels.size());

            // 2. Upsert each hotel into LOCAL database
            for (Map<String, Object> hotel : simatdaHotels) {
                try {
                    Integer simatdaId = hotel.get("simatda_id") != null ? ((Number) hotel.get("simatda_id")).intValue() : null;
                    if (simatdaId == null) continue;

                    String propertyName = strVal(hotel.get("property_name"));
                    String address = strVal(hotel.get("address"));
                    String ownerName = strVal(hotel.get("owner_name"));
                    if (ownerName == null || ownerName.isEmpty() || "-".equals(ownerName) || "0".equals(ownerName)) {
                        ownerName = strVal(hotel.get("wp_name"));
                    }
                    if (ownerName == null || ownerName.isEmpty() || "-".equals(ownerName) || "0".equals(ownerName)) {
                        ownerName = propertyName; // fallback to property name
                    }
                    String ownerPhone = strVal(hotel.get("owner_phone"));
                    if (ownerPhone == null || ownerPhone.isEmpty() || "0".equals(ownerPhone) || "00".equals(ownerPhone) || "-".equals(ownerPhone)) {
                        ownerPhone = strVal(hotel.get("phone"));
                    }
                    String kecamatan = strVal(hotel.get("kecamatan"));
                    String kelurahan = strVal(hotel.get("kelurahan"));
                    String latStr = strVal(hotel.get("latitude"));
                    String lonStr = strVal(hotel.get("longitude"));
                    BigDecimal lat = (latStr != null && !latStr.isEmpty()) ? new BigDecimal(latStr) : null;
                    BigDecimal lon = (lonStr != null && !lonStr.isEmpty()) ? new BigDecimal(lonStr) : null;
                    boolean isClosed = hotel.get("is_closed") != null && (
                        hotel.get("is_closed") instanceof Boolean ? (Boolean) hotel.get("is_closed")
                        : ((Number) hotel.get("is_closed")).intValue() == 1
                    );
                    Integer wpId = hotel.get("wp_id") != null ? ((Number) hotel.get("wp_id")).intValue() : null;
                    String npwpd = strVal(hotel.get("npwpd"));
                    String objectNumber = hotel.get("object_number") != null ? String.valueOf(hotel.get("object_number")) : null;

                    // Determine accommodation type from name heuristic
                    String accType = guessAccommodationType(propertyName);

                    // Check if already exists
                    Integer existingCount = pbjtJdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM hotel_accommodations WHERE simatda_id = ?",
                            Integer.class, simatdaId);

                    if (existingCount != null && existingCount > 0) {
                        // Update existing
                        pbjtJdbcTemplate.update("""
                                UPDATE hotel_accommodations SET
                                    property_name = ?, owner_name = ?, owner_phone = ?,
                                    address = ?, kelurahan = ?, kecamatan = ?,
                                    latitude = ?, longitude = ?, is_closed = ?,
                                    status = ?, simatda_wp_id = ?, npwpd = ?, object_number = ?,
                                    synced_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                                WHERE simatda_id = ?
                                """,
                                propertyName, ownerName, ownerPhone,
                                address, kelurahan, kecamatan,
                                lat, lon, isClosed,
                                isClosed ? "CLOSED" : "ACTIVE", wpId, npwpd, objectNumber, simatdaId);
                        updated++;
                    } else {
                        // Insert new
                        pbjtJdbcTemplate.update("""
                                INSERT INTO hotel_accommodations
                                (simatda_id, simatda_wp_id, accommodation_type, property_name,
                                 owner_name, owner_phone, address, kelurahan, kecamatan,
                                 kabupaten, latitude, longitude, is_closed, status,
                                 formalization_status, has_business_permit, has_tax_registration,
                                 npwpd, object_number, synced_at)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'KABUPATEN LUMAJANG', ?, ?, ?, ?, 'INFORMAL', false, false, ?, ?, CURRENT_TIMESTAMP)
                                """,
                                simatdaId, wpId, accType, propertyName,
                                ownerName, ownerPhone, address, kelurahan, kecamatan,
                                lat, lon, isClosed, isClosed ? "CLOSED" : "ACTIVE",
                                npwpd, objectNumber);
                        synced++;
                    }
                } catch (Exception e) {
                    log.warn("Error syncing hotel: {}", e.getMessage());
                    errors++;
                }
            }

            // 3. Sync realization data from SIMATDA
            int realizationSynced = syncRealizationFromSimatda();

            result.put("success", true);
            result.put("totalFromSimatda", simatdaHotels.size());
            result.put("newlySynced", synced);
            result.put("updated", updated);
            result.put("errors", errors);
            result.put("realizationRecordsSynced", realizationSynced);
            result.put("message", String.format("Synced %d new, updated %d, %d errors, %d realization records",
                    synced, updated, errors, realizationSynced));

        } catch (Exception e) {
            log.error("Error during SIMATDA sync: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        log.info("=== Hotel sync completed: {} new, {} updated, {} errors ===", synced, updated, errors);
        return result;
    }

    /**
     * Sync yearly realization data from SIMATDA → local hotel_realisasi table.
     * SIMATDA is READ-ONLY.
     */
    private int syncRealizationFromSimatda() {
        int count = 0;
        try {
            List<Map<String, Object>> realizationData = mysqlJdbcTemplate.queryForList("""
                    SELECT t.t_idwpobjek as simatda_objek_id,
                           t.t_periodepajak as tahun,
                           SUM(t.t_dasarpengenaan) as total_revenue,
                           SUM(t.t_jmlhpajak) as total_tax,
                           SUM(COALESCE(t.t_jmlhpembayaran, 0)) as total_payment,
                           COUNT(*) as transaction_count
                    FROM t_transaksi t
                    WHERE t.t_jenispajak = ?
                    GROUP BY t.t_idwpobjek, t.t_periodepajak
                    """, JENIS_HOTEL);

            log.info("Fetched {} realization records from SIMATDA", realizationData.size());

            for (Map<String, Object> row : realizationData) {
                try {
                    int simatdaObjekId = ((Number) row.get("simatda_objek_id")).intValue();
                    String tahun = String.valueOf(row.get("tahun"));
                    long totalRevenue = ((Number) row.get("total_revenue")).longValue();
                    long totalTax = ((Number) row.get("total_tax")).longValue();
                    long totalPayment = ((Number) row.get("total_payment")).longValue();
                    int txCount = ((Number) row.get("transaction_count")).intValue();

                    List<Map<String, Object>> localHotel = pbjtJdbcTemplate.queryForList(
                            "SELECT id FROM hotel_accommodations WHERE simatda_id = ?", simatdaObjekId);

                    if (localHotel.isEmpty()) continue;
                    long hotelId = ((Number) localHotel.get(0).get("id")).longValue();

                    pbjtJdbcTemplate.update("""
                            INSERT INTO hotel_realisasi (hotel_id, simatda_objek_id, tahun, total_revenue, total_tax, total_payment, transaction_count, synced_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                            ON CONFLICT (hotel_id, tahun) DO UPDATE SET
                                total_revenue = EXCLUDED.total_revenue,
                                total_tax = EXCLUDED.total_tax,
                                total_payment = EXCLUDED.total_payment,
                                transaction_count = EXCLUDED.transaction_count,
                                synced_at = CURRENT_TIMESTAMP
                            """,
                            hotelId, simatdaObjekId, tahun, totalRevenue, totalTax, totalPayment, txCount);
                    count++;
                } catch (Exception e) {
                    log.warn("Error syncing realization: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error syncing realization from SIMATDA: {}", e.getMessage());
        }
        return count;
    }

    // ==================== HELPERS ====================

    private List<Map<String, Object>> cleanRowMaps(List<Map<String, Object>> rows) {
        if (rows == null) return null;
        for (Map<String, Object> row : rows) {
            cleanRowMap(row);
        }
        return rows;
    }

    private Map<String, Object> cleanRowMap(Map<String, Object> row) {
        if (row == null) return null;
        Object photoUrls = row.get("photo_urls");
        if (photoUrls instanceof java.sql.Array) {
            try {
                row.put("photo_urls", (String[]) ((java.sql.Array) photoUrls).getArray());
            } catch (Exception e) {
                row.put("photo_urls", null);
            }
        }
        return row;
    }

    private String guessAccommodationType(String name) {
        if (name == null) return "HOTEL";
        String lower = name.toLowerCase();
        if (lower.contains("kos") || lower.contains("kost")) return "RUMAH_KOS";
        if (lower.contains("wisma") || lower.contains("guest")) return "WISMA";
        if (lower.contains("homestay") || lower.contains("home stay")) return "HOMESTAY";
        if (lower.contains("penginapan") || lower.contains("losmen")) return "PENGINAPAN";
        return "HOTEL";
    }

    private String strVal(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private String getStr(Map<String, Object> data, String key, String defaultVal) {
        Object v = data.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    private BigDecimal getNum(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }

    private Integer getInt(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private Boolean getBool(Map<String, Object> data, String key, Boolean defaultVal) {
        Object v = data.get(key);
        if (v == null) return defaultVal;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }

    @SuppressWarnings("unchecked")
    private String[] getPhotoUrls(Map<String, Object> data) {
        Object v = data.get("photo_urls");
        if (v == null) return null;
        if (v instanceof String[]) return (String[]) v;
        if (v instanceof List) {
            List<?> list = (List<?>) v;
            return list.stream().map(Object::toString).toArray(String[]::new);
        }
        return null;
    }
}
