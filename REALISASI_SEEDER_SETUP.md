# PBJT Realisasi Seeder Setup

## Overview

Mekanisme untuk membuat **seeder data realisasi** dari SIMATDA sehingga aplikasi PBJT tidak perlu koneksi ke SIMATDA setelah initial load.

## Architecture

```
SIMATDA (MySQL)              PBJT (PostgreSQL)
┌─────────────────┐          ┌──────────────────────────┐
│ t_transaksi     │          │ pbjt_assessments         │
│ - 2021-2025     │          │ - id                     │
│ - jenis PBJT    │  ────►   │ - tax_object_id          │
│                 │          │ - nop                    │
└─────────────────┘          └──────────────────────────┘
                                        │
                                        │ FK
                                        ▼
                             ┌──────────────────────────┐
                             │ pbjt_realisasi (NEW!)    │
                             │ - assessment_id          │
                             │ - tax_object_id          │
                             │ - tahun (2021-2025)      │
                             │ - realisasi_amount       │
                             │ - jumlah_transaksi       │
                             └──────────────────────────┘
```

## Database Schema

### Tabel: `pbjt_realisasi`

Tabel baru untuk menyimpan data realisasi per tahun dari SIMATDA.

```sql
CREATE TABLE pbjt_realisasi (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    tax_object_id VARCHAR(50),
    nop VARCHAR(50),
    business_name VARCHAR(255),
    tahun INTEGER NOT NULL,
    realisasi_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    jumlah_transaksi INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pbjt_realisasi_assessment 
        FOREIGN KEY (assessment_id) 
        REFERENCES pbjt_assessments(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_realisasi_assessment_tahun 
        UNIQUE (assessment_id, tahun)
);
```

## Setup Steps

### 1. Run Migration

Migration file sudah dibuat: `V5__create_pbjt_realisasi_table.sql`

```bash
# Migration akan otomatis jalan saat aplikasi start
./mvnw spring-boot:run
```

Atau manual:

```bash
$env:PGPASSWORD='root'; psql -h localhost -U postgres -d pbjt_assessment_db -f src/main/resources/db/migration/V5__create_pbjt_realisasi_table.sql
```

### 2. Generate Realisasi Seeder

Jalankan utility class untuk generate SQL seeder:

```bash
# Compile dulu
./mvnw clean compile

# Run generator
./mvnw exec:java -Dexec.mainClass="com.example.leaflet_geo.util.GenerateRealisasiSeeder" -Dexec.cleanupDaemonThreads=false > pbjt_realisasi_seeder.sql
```

Output akan berisi:
- `TRUNCATE TABLE pbjt_realisasi;`
- Multiple `INSERT INTO pbjt_realisasi ...` statements
- Summary statistics

### 3. Load Seeder Data

```bash
# Load seeder ke database
$env:PGPASSWORD='root'; psql -h localhost -U postgres -d pbjt_assessment_db -f pbjt_realisasi_seeder.sql
```

### 4. Verify Data

```sql
-- Summary per tahun
SELECT 
    tahun,
    COUNT(*) as jumlah_objek,
    SUM(realisasi_amount) as total_realisasi,
    SUM(jumlah_transaksi) as total_transaksi,
    AVG(realisasi_amount) as avg_realisasi
FROM pbjt_realisasi
GROUP BY tahun
ORDER BY tahun;

-- Detail realisasi untuk satu assessment
SELECT 
    a.business_id,
    a.business_name,
    r.tahun,
    r.realisasi_amount,
    r.jumlah_transaksi
FROM pbjt_assessments a
JOIN pbjt_realisasi r ON a.id = r.assessment_id
WHERE a.business_id = 'SIM-0001'
ORDER BY r.tahun;

-- Total realisasi 5 tahun per assessment
SELECT 
    a.business_id,
    a.business_name,
    SUM(r.realisasi_amount) as total_realisasi_5tahun,
    SUM(r.jumlah_transaksi) as total_transaksi_5tahun,
    AVG(r.realisasi_amount) as avg_realisasi_pertahun
FROM pbjt_assessments a
JOIN pbjt_realisasi r ON a.id = r.assessment_id
GROUP BY a.id, a.business_id, a.business_name
ORDER BY total_realisasi_5tahun DESC
LIMIT 20;
```

## Benefits

✅ **No SIMATDA Dependency**: Setelah seeder diload, aplikasi tidak perlu koneksi ke SIMATDA lagi  
✅ **Better Performance**: Query langsung ke PostgreSQL, tidak perlu remote connection  
✅ **Historical Data**: Data realisasi tersimpan permanen per tahun  
✅ **Easier Testing**: Bisa test dengan data lokal tanpa depend on external DB  
✅ **Data Consistency**: Data frozen pada saat seeder dibuat, tidak berubah-ubah  

## API Integration (Next Step)

Setelah data realisasi ada di database lokal, bisa dibuat endpoint:

```java
// GET /api/assessments/{id}/realisasi
// Response:
{
  "assessmentId": 1,
  "businessId": "SIM-0001",
  "businessName": "Restaurant ABC",
  "realisasi": [
    {"tahun": 2021, "amount": 5000000, "transaksi": 120},
    {"tahun": 2022, "amount": 5500000, "transaksi": 130},
    {"tahun": 2023, "amount": 6000000, "transaksi": 145},
    {"tahun": 2024, "amount": 6500000, "transaksi": 150},
    {"tahun": 2025, "amount": 7000000, "transaksi": 160}
  ],
  "totalRealisasi": 30000000,
  "avgRealisasiPerTahun": 6000000
}
```

## Update Strategy

Untuk update data realisasi (misal ada data baru):

1. Re-run `GenerateRealisasiSeeder.java`
2. Output akan `TRUNCATE` dan insert ulang semua data
3. Atau bisa dibuat incremental update untuk tahun tertentu saja

## Files Created

- `V5__create_pbjt_realisasi_table.sql` - Migration schema
- `GenerateRealisasiSeeder.java` - Generator utility
- `REALISASI_SEEDER_SETUP.md` - This documentation
