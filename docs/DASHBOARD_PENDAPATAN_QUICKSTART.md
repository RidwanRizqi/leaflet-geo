# Dashboard Pendapatan Pajak - Quick Start Guide

## 📋 Overview
Dashboard visualisasi Target vs Realisasi Pendapatan Pajak yang terintegrasi dengan database SIMATDA (simpatda_lumajang).

## ✅ Yang Sudah Dibuat

### Backend (Spring Boot)

#### 1. **Konfigurasi Database**
- File: `src/main/resources/application.properties`
- Koneksi MySQL SIMATDA sudah ditambahkan
- Database: `simpatda_lumajang` di localhost:3306

#### 2. **DTO (Data Transfer Objects)**
- `TargetRealisasiDTO.java` - Data target vs realisasi per jenis pajak
- `DashboardSummaryDTO.java` - Summary data dashboard
- `TrendBulananDTO.java` - Data trend bulanan
- `TopKontributorDTO.java` - Data top kontributor

#### 3. **Service Layer**
- `PendapatanService.java`
  - `getDashboardSummary(tahun)` - Ambil summary data
  - `getTargetRealisasiPerJenis(tahun)` - Target vs realisasi per jenis pajak
  - `getTrendBulanan(tahun)` - Trend realisasi bulanan (kumulatif)
  - `getTopKontributor(tahun, limit)` - Top 10 wajib pajak kontributor
  - `getRealisasiByJenisPajak(tahun, jenisPajakId)` - Detail realisasi per jenis

#### 4. **Controller/API Endpoints**
- `PendapatanController.java`
- Base URL: `/api/pendapatan`

**Endpoints:**
```
GET  /api/pendapatan/summary?tahun=2025
GET  /api/pendapatan/target-realisasi?tahun=2025
GET  /api/pendapatan/trend-bulanan?tahun=2025
GET  /api/pendapatan/top-kontributor?tahun=2025&limit=10
GET  /api/pendapatan/realisasi-by-jenis?tahun=2025&jenisPajakId=1
```

### Frontend (Angular)

#### 1. **Models**
- File: `src/app/core/models/pendapatan.model.ts`
- Interface TypeScript untuk semua DTO

#### 2. **Service**
- File: `src/app/core/services/pendapatan.service.ts`
- Service untuk consume API backend

#### 3. **Component**
- Path: `src/app/pages/dashboard-pendapatan/`
- Standalone component dengan routing

#### 4. **Routing**
- URL: `/#/dashboard-pendapatan`
- Sudah ditambahkan di `app-routing.module.ts`

## 🚀 Cara Menjalankan

### 1. Start Backend

```powershell
cd d:\BPRD\leaflet-geo\leaflet-geo
.\mvnw.cmd spring-boot:run
```

Backend akan berjalan di: `http://localhost:8080`

### 2. Test API Endpoint

Buka browser atau Postman, test endpoint:
```
http://localhost:8080/api/pendapatan/summary?tahun=2025
```

### 3. Start Frontend

```powershell
cd d:\BPRD\leaflet-geo\leaflet-geo-FE
npm start
```

Frontend akan berjalan di: `http://localhost:4200`

### 4. Akses Dashboard

Buka browser:
```
http://localhost:4200/#/dashboard-pendapatan
```

## 📊 Fitur Dashboard

### 1. **Summary Cards**
- Total Target
- Total Realisasi (dengan % pencapaian)
- Total Wajib Pajak & Objek Pajak
- Total Transaksi & Jenis Pajak Aktif

### 2. **Bar Chart - Target vs Realisasi**
- Perbandingan target dan realisasi per jenis pajak
- Visualisasi side-by-side
- Tooltip dengan detail rupiah

### 3. **Progress Bars**
- Pencapaian target per jenis pajak
- Color coding:
  - ≥ 90%: Success (hijau)
  - ≥ 70%: Info (biru)
  - ≥ 50%: Warning (kuning)
  - < 50%: Danger (merah)

### 4. **Line Chart - Trend Bulanan**
- Realisasi kumulatif dari Januari - Desember
- Smooth line chart
- Tooltip dengan detail per bulan

### 5. **Table - Top 10 Kontributor**
- NPWPD
- Nama Wajib Pajak
- Jenis Pajak
- Total Pembayaran
- Jumlah Transaksi

### 6. **Filter Tahun**
- Dropdown untuk pilih tahun
- Data akan refresh otomatis

## 🗄️ Database Tables yang Digunakan

### Target (Anggaran)
- `s_target` - Header target per tahun
- `s_targetdetail` - Detail target per rekening
- `s_targetjenis` - Jenis target (APBD/RAPBD)

### Realisasi (Penerimaan)
- `t_transaksi` - Transaksi pembayaran utama
- `t_skpdkb` - SKPD Kurang Bayar
- `t_skpdkbt` - SKPD Kurang Bayar Tambahan
- `t_skpdt` - SKPD Tambahan
- `t_angsuran` - Pembayaran angsuran

### Master Data
- `s_rekening` / `view_rekening` - Kode rekening pajak
- `s_jenisobjek` - Jenis pajak (Hotel, Restoran, dll)
- `t_wp` / `view_wp` - Wajib Pajak
- `t_wpobjek` / `view_wpobjek` - Objek Pajak

## 🔧 Troubleshooting

### Backend Tidak Bisa Connect ke Database

1. Pastikan MySQL berjalan
2. Cek credential di `application.properties`:
   ```properties
   spring.datasource.mysql.url=jdbc:mysql://localhost:3306/simpatda_lumajang
   spring.datasource.mysql.username=root
   spring.datasource.mysql.password=
   ```

3. Test koneksi manual:
   ```powershell
   mysql -u root -h localhost simpatda_lumajang
   ```

### API Return Empty Data

1. Cek apakah ada data di database untuk tahun yang dipilih
2. Query manual untuk test:
   ```sql
   SELECT * FROM s_target WHERE s_tahuntarget = 2025;
   SELECT * FROM t_transaksi WHERE YEAR(t_tglpembayaran) = 2025 LIMIT 10;
   ```

### Frontend Error: Can't resolve service

1. Pastikan service sudah di-provide:
   ```typescript
   @Injectable({
     providedIn: 'root'
   })
   ```

2. Atau tambahkan di providers array jika menggunakan module-based component

### Chart Tidak Muncul

1. Pastikan ng-apexcharts sudah terinstall:
   ```powershell
   npm install ng-apexcharts apexcharts --save
   ```

2. Check import di component:
   ```typescript
   imports: [CommonModule, FormsModule, NgApexchartsModule]
   ```

## 📝 Next Steps / Future Enhancements

1. **Export to Excel/PDF**
   - Button untuk download laporan

2. **Filter Advanced**
   - Filter per jenis pajak
   - Filter per periode (bulan, triwulan)
   - Filter per kecamatan

3. **Drill-down Detail**
   - Click jenis pajak untuk lihat detail
   - List transaksi per wajib pajak

4. **Map Integration**
   - Visualisasi realisasi per kecamatan di peta
   - Chloropleth berdasarkan % pencapaian

5. **Real-time Updates**
   - WebSocket untuk update otomatis
   - Notifikasi target tercapai

6. **Comparison**
   - Compare antar tahun
   - Proyeksi pencapaian target

## 🔗 Related Documents

- `SIMATDA_TARGET_REALISASI_QUERIES.md` - SQL queries detail
- `DATABASE_SETUP.md` - Setup database
- `DASHBOARD_PAJAK_README.md` - Dashboard pajak yang sudah ada

## 💡 Tips

1. **Performance**: Untuk data besar, pertimbangkan:
   - Indexing pada kolom `t_tglpembayaran`, `t_jenispajak`
   - Materialized view untuk summary data
   - Caching di backend (Redis)

2. **Data Accuracy**: 
   - Pastikan semua tabel realisasi (t_transaksi, t_skpdkb, dll) sudah ter-agregasi
   - Validasi data double payment

3. **User Experience**:
   - Loading indicator untuk setiap section
   - Error handling yang informatif
   - Responsive design untuk mobile

---

**Created**: 2025-11-11  
**Last Updated**: 2025-11-11  
**Version**: 1.0.0
