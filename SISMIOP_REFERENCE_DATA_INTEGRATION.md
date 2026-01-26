# PBJT Assessment - SISMIOP Reference Data Integration

## Overview
Integrated accurate kecamatan (kd_kec) and kelurahan (kd_kel) codes from SISMIOP Oracle database into PBJT Assessment records for seamless joining with other BPRD systems.

## Implementation Summary

### 1. Database Schema Updates
Added two new columns to `pbjt_assessments` table:
- `kd_kec VARCHAR(10)` - Kecamatan code from SISMIOP REF_KECAMATAN
- `kd_kel VARCHAR(10)` - Kelurahan code from SISMIOP REF_KELURAHAN

```sql
ALTER TABLE pbjt_assessments ADD COLUMN kd_kec VARCHAR(10);
ALTER TABLE pbjt_assessments ADD COLUMN kd_kel VARCHAR(10);
```

### 2. Oracle SISMIOP Mapping

#### Kecamatan Mapping (21 kecamatan in Lumajang)
Mapped all 21 kecamatan from `REF_KECAMATAN` table:

| Kecamatan Name | kd_kec | Assessments |
|---------------|--------|-------------|
| LUMAJANG | 060 | 55 |
| SUKODONO | 120 | 13 |
| TEMPEH | 050 | 10 |
| YOSOWILANGUN | 090 | 9 |
| CANDIPURO | 030 | 8 |
| PASIRIAN | 040 | 7 |
| SENDURO | 130 | 5 |
| KEDUNGJAJANG | 151 | 4 |
| PRONOJIWO | 020 | 4 |
| KUNIR | 080 | 4 |
| SUMBERSUKO | 061 | 4 |
| TEMPURSARI | 010 | 3 |
| TEKUNG | 050 | 4 |
| RANDUAGUNG | 110 | 2 |
| PASRUJAMBE | 131 | 2 |
| GUCIALIT | 140 | 2 |
| JATIROTO | 100 | 2 |
| KLAKAH | 150 | 2 |
| PADANG | 121 | 1 |
| RANUYOSO | 160 | 1 |
| ROWOKANGKUNG | 101 | 1 |

**Coverage: 100% (143 out of 143 records have kd_kec)**

#### Kelurahan Mapping
Mapped kelurahan codes from `REF_KELURAHAN` table using the following join:

```sql
SELECT k.KD_KECAMATAN, k.KD_KELURAHAN,
       kec.NM_KECAMATAN, k.NM_KELURAHAN
FROM REF_KELURAHAN k
JOIN REF_KECAMATAN kec ON k.KD_PROPINSI = kec.KD_PROPINSI 
    AND k.KD_DATI2 = kec.KD_DATI2 
    AND k.KD_KECAMATAN = kec.KD_KECAMATAN
ORDER BY k.KD_KECAMATAN, k.KD_KELURAHAN;
```

**Coverage: 96.5% (138 out of 143 records have kd_kel)**

#### Missing kd_kel Records (5 records)
The following 5 records don't have kd_kel because their kelurahan names don't exist in SISMIOP REF_KELURAHAN:

| ID | Kecamatan | Kelurahan | kd_kec | Reason |
|----|-----------|-----------|--------|--------|
| 35, 51 | SUKODONO | SUKODONO | 120 | Kelurahan "SUKODONO" not in REF_KELURAHAN |
| 34, 49, 50 | TEMPEH | TEMPEH | 050 | Kelurahan "TEMPEH" not in REF_KELURAHAN (only TEMPEHLOR, TEMPEHTENGAH, TEMPEHKIDUL exist) |

These are administrative units where the kecamatan name matches the kelurahan name, but they're not registered as separate kelurahan in SISMIOP.

### 3. Data Challenges & Solutions

#### Challenge 1: Name Variations with Spaces
**Problem:** Some kecamatan/kelurahan names had spaces inserted (e.g., "K L A K A H" vs "KLAKAH")

**Solution:** Used `UPPER(REPLACE(name, ' ', ''))` for matching:
```sql
UPDATE pbjt_assessments 
SET kd_kec = '150' 
WHERE UPPER(REPLACE(kecamatan, ' ', '')) = 'KLAKAH';
```

#### Challenge 2: Incorrect Initial kd_kec Assignment
**Problem:** Some records had wrong kecamatan codes initially

**Examples:**
- DOROGOWOK was in kd_kec 010 → Should be 080 (KUNIR)
- YOSOWILANGUN related kelurahan in kd_kec 040 → Should be 090
- TEMPURSARI in kd_kec 070 → Should be 010

**Solution:** Created comprehensive correction script (`fix_kd_kel_mismatches.sql`) to reassign correct kecamatan codes before kelurahan mapping.

#### Challenge 3: Kelurahan Name Variations
**Problem:** Name variations like "SUMBER REJO" vs "SUMBERREJO"

**Solution:** Normalized all names using `UPPER(REPLACE(kelurahan, ' ', ''))` for matching.

### 4. Backend Code Updates

#### Entity Update: PbjtAssessment.java
Added kd_kel field:
```java
@Column(name = "kd_kec", length = 10)
private String kdKec;  // Kode kecamatan untuk drill-down ke kelurahan

@Column(name = "kd_kel", length = 10)
private String kdKel;  // Kode kelurahan dari SISMIOP untuk join akurat
```

#### DTO Update: PbjtLocationStatsDTO.java
Added kdKel field to statistics DTO:
```java
private String kecamatan;
private String kdKec;  // Kode kecamatan untuk drill-down
private String kdKel;  // Kode kelurahan dari SISMIOP
private String kelurahan;
```

#### Service Update: PbjtAssessmentService.java
Updated `buildLocationStats()` to populate kdKel:
```java
// Get kdKel from first assessment that has it
String kdKel = assessments.stream()
    .filter(a -> a.getKdKel() != null && !a.getKdKel().isEmpty())
    .findFirst()
    .map(PbjtAssessment::getKdKel)
    .orElse(null);

return PbjtLocationStatsDTO.builder()
    .kecamatan(kecamatan)
    .kdKec(kdKec)
    .kdKel(kdKel)  // New field
    .kelurahan(kelurahan)
    // ... other fields
    .build();
```

### 5. SQL Scripts Created

1. **add_kd_kec_column.sql** - Added kd_kec column to database
2. **update_kd_kec_mapping.sql** - Mapped 21 kecamatan codes from SISMIOP
3. **fix_kd_kec_spaces.sql** - Fixed kecamatan names with spaces
4. **add_kd_kel_column.sql** - Added kd_kel column to database
5. **query_kelurahan.sql** - Oracle query to get kelurahan mapping
6. **update_kd_kel_mapping.sql** - Initial kelurahan code mapping
7. **fix_kd_kel_mismatches.sql** - Fixed incorrect kecamatan assignments
8. **final_kd_kel_fix.sql** - Final corrections for remaining records
9. **final_summary.sql** - Statistics and coverage report

### 6. Usage & Benefits

#### For API Consumers
The API now returns kd_kec and kd_kel in all endpoints:

```json
{
  "kecamatan": "LUMAJANG",
  "kdKec": "060",
  "kdKel": "018",
  "kelurahan": "TOMPOKERSAN",
  "jumlahUsaha": 14,
  "totalAnnualPbjt": 1500000000
}
```

#### For System Integration
These codes enable direct joining with:
- SISMIOP PBB system (Property Tax)
- SIMATDA Pajak Daerah (Regional Tax System)
- BPHTB (Land and Building Transfer Tax)
- SIG (Geographic Information System)

Example join:
```sql
SELECT 
    pa.business_name,
    pa.kd_kec,
    pa.kd_kel,
    rk.nm_kelurahan AS kelurahan_name_from_sismiop,
    -- More fields from other systems
FROM pbjt_assessments pa
LEFT JOIN SISMIOP.REF_KELURAHAN rk 
    ON rk.kd_kecamatan = pa.kd_kec 
    AND rk.kd_kelurahan = pa.kd_kel;
```

#### For Map Drill-Down
The seamless navigation feature uses kdKec for:
1. Finding kecamatan boundaries from BPRD API
2. Loading all kelurahan within that kecamatan
3. Highlighting the specific business location
4. Showing all other businesses in the same kelurahan

### 7. Verification Queries

Check coverage by kecamatan:
```sql
SELECT 
    kd_kec,
    kecamatan,
    COUNT(*) as total,
    COUNT(kd_kel) as with_kd_kel,
    COUNT(*) - COUNT(kd_kel) as missing_kd_kel
FROM pbjt_assessments 
WHERE kd_kec IS NOT NULL
GROUP BY kd_kec, kecamatan
ORDER BY kd_kec;
```

Overall statistics:
```sql
SELECT 
    COUNT(*) as total_records,
    COUNT(kd_kec) as with_kd_kec,
    COUNT(kd_kel) as with_kd_kel,
    ROUND(100.0 * COUNT(kd_kec) / COUNT(*), 2) as pct_kd_kec,
    ROUND(100.0 * COUNT(kd_kel) / COUNT(*), 2) as pct_kd_kel
FROM pbjt_assessments;
```

### 8. Results

**Final Coverage:**
- kd_kec: 143/143 (100%)
- kd_kel: 138/143 (96.5%)

**Impact:**
- ✅ All PBJT assessment records can be joined with SISMIOP by kecamatan
- ✅ 96.5% of records can be joined with SISMIOP by kelurahan
- ✅ Map drill-down now uses accurate SISMIOP codes
- ✅ Cross-system data integration is now seamless

## References

- SISMIOP Oracle Database: `192.178.10.101:1521/SISMIOP`
- SISMIOP User: `PBB` / `PBB`
- Reference Tables: `REF_KECAMATAN`, `REF_KELURAHAN`
- PBJT Database: `localhost:5432/pbjt_assessment_db`

## Future Enhancements

1. **Periodic Sync:** Create scheduled job to sync new kelurahan additions from SISMIOP
2. **Manual Override:** Add UI for manually setting kd_kel for the 5 records without matches
3. **Validation:** Add validation to ensure kd_kec and kd_kel match kecamatan and kelurahan names
4. **Audit Trail:** Track when codes were added/updated for each record
