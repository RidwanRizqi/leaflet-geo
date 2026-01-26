# Database Setup Instructions

## PostgreSQL Configuration

### 1. Database Connection
- **Host**: localhost
- **Port**: 5432 (default)
- **Database**: postgres
- **Username**: postgres
- **Password**: root

### 2. Setup Steps

#### Step 1: Create Database Schema
Jalankan script SQL berikut di PostgreSQL:

```sql
-- Create schema
CREATE SCHEMA IF NOT EXISTS sig;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

#### Step 2: Run Table Creation Script
Jalankan file `src/main/resources/sql/schema.sql` di PostgreSQL untuk membuat tabel dan data sample.

#### Step 3: Verify Setup
Setelah aplikasi Spring Boot berjalan, test koneksi database dengan mengakses:
```
GET http://localhost:8080/api/bidang/health
```

### 3. API Endpoints

#### Health Check
- `GET /api/bidang/health` - Test database connection

#### Data Retrieval
- `GET /api/bidang` - Get all active bidang records
- `GET /api/bidang/{id}` - Get bidang by ID
- `GET /api/bidang/nop/{nop}` - Get bidang by NOP
- `GET /api/bidang/geometry` - Get all bidang with geometry data

#### Geographic Queries
- `GET /api/bidang/province/{kdProp}` - Get by province code
- `GET /api/bidang/district/{kdProp}/{kdDati2}` - Get by district code
- `GET /api/bidang/subdistrict/{kdProp}/{kdDati2}/{kdKec}` - Get by sub-district code
- `GET /api/bidang/village/{kdProp}/{kdDati2}/{kdKec}/{kdKel}` - Get by village code

#### Search
- `GET /api/bidang/search?nop={partialNop}` - Search by partial NOP

### 4. Sample Data
Tabel sudah diisi dengan data sample:
- **ID**: 92523f4a-7722-4acd-ac63-5085f964dcd0
- **NOP**: 35.08.130.017.005.0139.0
- **Geometry**: WKT format geometry data

### 5. Troubleshooting

#### Database Connection Issues
1. Pastikan PostgreSQL service berjalan
2. Cek username/password di `application.properties`
3. Pastikan database `postgres` sudah ada
4. Cek port 5432 tidak digunakan aplikasi lain

#### Schema Issues
1. Pastikan schema `sig` sudah dibuat
2. Pastikan extension `uuid-ossp` sudah diinstall
3. Cek permissions user `postgres`

#### Application Issues
1. Cek log aplikasi untuk error details
2. Pastikan semua dependencies terinstall
3. Restart aplikasi setelah perubahan konfigurasi

