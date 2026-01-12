# DAT_OBJEK_PAJAK & DAT_SUBJEK_PAJAK API Documentation

## Overview
API endpoints untuk mengakses data objek pajak dan subjek pajak dari database Oracle.

## Tables
- **DAT_OBJEK_PAJAK**: Data objek pajak dengan relasi ke subjek pajak
- **DAT_SUBJEK_PAJAK**: Data subjek pajak (wajib pajak)

## DAT_OBJEK_PAJAK Endpoints

### 1. Get All Data (Paginated)
```
GET /api/dat-objek-pajak?page=0&size=10
```
**Response:**
```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 1000,
    "totalPages": 100,
    "hasNext": true,
    "hasPrev": false
  },
  "success": true,
  "message": "Data objek pajak berhasil diambil"
}
```

### 2. Get Count
```
GET /api/dat-objek-pajak/count
```
**Response:**
```json
{
  "success": true,
  "message": "Count berhasil diambil",
  "totalCount": 1000
}
```

### 3. Get By Primary Key (Complete ID)
```
GET /api/dat-objek-pajak/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{kdBlok}/{noUrut}/{kdJnsOp}
```
**Example:**
```
GET /api/dat-objek-pajak/35/08/010/001/001/0001/1
```
**Response:**
```json
{
  "objekPajak": {
    "kdPropinsi": "35",
    "kdDati2": "08",
    "kdKecamatan": "010",
    "kdKelurahan": "001",
    "kdBlok": "001",
    "noUrut": "0001",
    "kdJnsOp": "1",
    "subjekPajakId": "SP001",
    "noFormulirSpop": "12345678901",
    "totalLuasBumi": 100.50,
    "totalLuasBng": 75.25,
    "njopBumi": 50000000,
    "njopBng": 75000000,
    "latitude": "-7.250445",
    "longitude": "112.768845"
  },
  "subjekPajak": {
    "subjekPajakId": "SP001",
    "nmWp": "John Doe",
    "npwp": "123456789012345",
    "jalanWp": "Jl. Contoh No. 123",
    "kotaWp": "Lumajang",
    "email": "john@example.com"
  },
  "success": true,
  "message": "Data objek pajak berhasil diambil"
}
```

### 4. Get By Kecamatan (Paginated)
```
GET /api/dat-objek-pajak/kecamatan/{kdPropinsi}/{kdDati2}/{kdKecamatan}?page=0&size=10
```
**Example:**
```
GET /api/dat-objek-pajak/kecamatan/35/08/010?page=0&size=10
```

### 5. Get By Kelurahan (Paginated)
```
GET /api/dat-objek-pajak/kelurahan/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}?page=0&size=10
```
**Example:**
```
GET /api/dat-objek-pajak/kelurahan/35/08/010/001?page=0&size=10
```

### 6. Get Detail By Kecamatan, Kelurahan, No Urut
```
GET /api/dat-objek-pajak/detail/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{noUrut}
```
**Example:**
```
GET /api/dat-objek-pajak/detail/35/08/010/001/0001
```
**Response:**
```json
{
  "data": [
    {
      "kdPropinsi": "35",
      "kdDati2": "08",
      "kdKecamatan": "010",
      "kdKelurahan": "001",
      "kdBlok": "001",
      "noUrut": "0001",
      "kdJnsOp": "1",
      "subjekPajakId": "SP001",
      "totalLuasBumi": 100.50,
      "totalLuasBng": 75.25,
      "njopBumi": 50000000,
      "njopBng": 75000000
    },
    {
      "kdPropinsi": "35",
      "kdDati2": "08",
      "kdKecamatan": "010",
      "kdKelurahan": "001",
      "kdBlok": "001",
      "noUrut": "0001",
      "kdJnsOp": "2",
      "subjekPajakId": "SP001",
      "totalLuasBumi": 200.00,
      "totalLuasBng": 150.00,
      "njopBumi": 100000000,
      "njopBng": 150000000
    }
  ],
  "filters": {
    "kdPropinsi": "35",
    "kdDati2": "08",
    "kdKecamatan": "010",
    "kdKelurahan": "001",
    "noUrut": "0001"
  },
  "success": true,
  "message": "Data objek pajak berhasil diambil",
  "totalCount": 2
}
```

### 7. Get By Subjek Pajak ID
```
GET /api/dat-objek-pajak/subjek-pajak/{subjekPajakId}
```
**Example:**
```
GET /api/dat-objek-pajak/subjek-pajak/SP001
```

### 8. Get By NPWP
```
GET /api/dat-objek-pajak/npwp/{npwp}
```
**Example:**
```
GET /api/dat-objek-pajak/npwp/123456789012345
```

### 9. Get By No Formulir SPOP
```
GET /api/dat-objek-pajak/no-formulir-spop/{noFormulirSpop}
```
**Example:**
```
GET /api/dat-objek-pajak/no-formulir-spop/12345678901
```

## DAT_SUBJEK_PAJAK Endpoints

### 1. Get All Data (Paginated)
```
GET /api/dat-subjek-pajak?page=0&size=10
```

### 2. Get Count
```
GET /api/dat-subjek-pajak/count
```

### 3. Get By ID
```
GET /api/dat-subjek-pajak/{subjekPajakId}
```
**Example:**
```
GET /api/dat-subjek-pajak/SP001
```

### 4. Get By NPWP
```
GET /api/dat-subjek-pajak/npwp/{npwp}
```
**Example:**
```
GET /api/dat-subjek-pajak/npwp/123456789012345
```

### 5. Get By NPWPD
```
GET /api/dat-subjek-pajak/npwpd/{npwpd}
```
**Example:**
```
GET /api/dat-subjek-pajak/npwpd/NPWPD123456
```

### 6. Search By Name
```
GET /api/dat-subjek-pajak/search/name?nmWp=John
```

### 7. Get By Email
```
GET /api/dat-subjek-pajak/email/{email}
```
**Example:**
```
GET /api/dat-subjek-pajak/email/john@example.com
```

### 8. Get By Kota
```
GET /api/dat-subjek-pajak/kota/{kotaWp}
```
**Example:**
```
GET /api/dat-subjek-pajak/kota/Lumajang
```

### 9. Get By Kelurahan
```
GET /api/dat-subjek-pajak/kelurahan/{kelurahanWp}
```
**Example:**
```
GET /api/dat-subjek-pajak/kelurahan/Tempur Sari
```

## Key Features

### 1. **Relational Data**
- Endpoint utama (`/api/dat-objek-pajak/{id}`) mengembalikan data objek pajak beserta data subjek pajak terkait
- Data subjek pajak diambil berdasarkan `SUBJEK_PAJAK_ID` dari objek pajak

### 2. **Flexible Filtering**
- Filter berdasarkan kecamatan, kelurahan, no urut
- Filter berdasarkan NPWP, NPWPD, email
- Search berdasarkan nama wajib pajak

### 3. **Pagination Support**
- Semua endpoint list mendukung pagination
- Parameter: `page` (default: 0), `size` (default: 10)
- Response includes pagination metadata

### 4. **Error Handling**
- Consistent error response format
- HTTP status codes: 200 (success), 404 (not found), 500 (server error)
- Error messages dalam bahasa Indonesia

## Database Schema

### DAT_OBJEK_PAJAK Primary Key
- KD_PROPINSI (CHAR(2))
- KD_DATI2 (CHAR(2))
- KD_KECAMATAN (CHAR(3))
- KD_KELURAHAN (CHAR(3))
- KD_BLOK (CHAR(3))
- NO_URUT (CHAR(4))
- KD_JNS_OP (CHAR)

### DAT_SUBJEK_PAJAK Primary Key
- SUBJEK_PAJAK_ID (CHAR(30))

### Foreign Key Relationship
- DAT_OBJEK_PAJAK.SUBJEK_PAJAK_ID → DAT_SUBJEK_PAJAK.SUBJEK_PAJAK_ID

## Usage Examples

### Get Detail Objek Pajak dengan Subjek Pajak
```bash
curl "http://localhost:8080/api/dat-objek-pajak/35/08/010/001/001/0001/1"
```

### Get All Objek Pajak by Kecamatan
```bash
curl "http://localhost:8080/api/dat-objek-pajak/kecamatan/35/08/010?page=0&size=20"
```

### Get All Objek Pajak by Kelurahan
```bash
curl "http://localhost:8080/api/dat-objek-pajak/kelurahan/35/08/010/001?page=0&size=20"
```

### Get Detail by Kecamatan, Kelurahan, No Urut
```bash
curl "http://localhost:8080/api/dat-objek-pajak/detail/35/08/010/001/0001"
```

### Search Subjek Pajak by Name
```bash
curl "http://localhost:8080/api/dat-subjek-pajak/search/name?nmWp=John"
```

### Get Subjek Pajak by NPWP
```bash
curl "http://localhost:8080/api/dat-subjek-pajak/npwp/123456789012345"
```

## Response Format

All endpoints return JSON with consistent structure:
```json
{
  "data": [...],           // Array of data (for list endpoints)
  "objekPajak": {...},     // Single object (for detail endpoints)
  "subjekPajak": {...},    // Related subjek pajak data
  "pagination": {...},     // Pagination info (for paginated endpoints)
  "filters": {...},        // Applied filters
  "success": true,         // Success status
  "message": "...",        // Response message
  "totalCount": 100        // Total count (for some endpoints)
}
