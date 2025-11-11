# Query Database SIMATDA - Target vs Realisasi

## 1. Query Target Per Tahun

```sql
-- Target Per Tahun dan Per Jenis Pajak
SELECT 
    t.s_tahuntarget AS tahun,
    tj.s_namatargetjenis AS jenis_target,
    r.s_namakorek AS nama_rekening,
    j.s_namajenis AS jenis_pajak,
    td.s_targetjumlah AS target_rupiah
FROM s_target t
JOIN s_targetdetail td ON t.s_idtarget = td.s_idtargetheader
JOIN s_targetjenis tj ON t.s_statustarget = tj.s_idtargetjenis
LEFT JOIN s_rekening r ON td.s_targetrekening = r.s_idkorek
LEFT JOIN s_jenisobjek j ON r.s_jenisobjek = j.s_idjenis
WHERE t.s_tahuntarget = 2025
ORDER BY j.s_order, r.s_namakorek;
```

## 2. Query Realisasi Per Tahun

```sql
-- Realisasi dari Transaksi Utama
SELECT 
    YEAR(t.t_tglpembayaran) AS tahun,
    MONTH(t.t_tglpembayaran) AS bulan,
    j.s_namajenis AS jenis_pajak,
    r.s_namakorek AS nama_rekening,
    SUM(t.t_jmlhpembayaran) AS realisasi_rupiah,
    COUNT(t.t_idtransaksi) AS jumlah_transaksi
FROM t_transaksi t
LEFT JOIN s_rekening r ON t.t_idkorek = r.s_idkorek
LEFT JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
WHERE YEAR(t.t_tglpembayaran) = 2025
GROUP BY YEAR(t.t_tglpembayaran), MONTH(t.t_tglpembayaran), 
         j.s_namajenis, r.s_namakorek
ORDER BY tahun, bulan, j.s_order;
```

## 3. Query Realisasi Total (All Sources)

```sql
-- Gabungan semua sumber realisasi
SELECT 
    YEAR(tgl) AS tahun,
    MONTH(tgl) AS bulan,
    jenis_pajak,
    SUM(jumlah) AS total_realisasi
FROM (
    -- Dari t_transaksi
    SELECT 
        t_tglpembayaran AS tgl,
        t_jenispajak AS jenis_pajak,
        t_jmlhpembayaran AS jumlah
    FROM t_transaksi
    
    UNION ALL
    
    -- Dari t_skpdkb
    SELECT 
        t_tglbayarskpdkb AS tgl,
        t_jenispajak AS jenis_pajak,
        t_jmlhbayarskpdkb AS jumlah
    FROM t_skpdkb
    WHERE t_tglbayarskpdkb IS NOT NULL
    
    UNION ALL
    
    -- Dari t_skpdkbt
    SELECT 
        t_tglbayarskpdkbt AS tgl,
        t_jenispajak AS jenis_pajak,
        t_jmlhbayarskpdkbt AS jumlah
    FROM t_skpdkbt
    WHERE t_tglbayarskpdkbt IS NOT NULL
    
    UNION ALL
    
    -- Dari t_skpdt
    SELECT 
        t_tglbayarskpdt AS tgl,
        t_jenispajak AS jenis_pajak,
        t_jmlhbayarskpdt AS jumlah
    FROM t_skpdt
    WHERE t_tglbayarskpdt IS NOT NULL
    
    UNION ALL
    
    -- Dari t_angsuran
    SELECT 
        t_tglpembayaranangsuran AS tgl,
        t_jenispajak AS jenis_pajak,
        t_pokokpembayaranangsuran AS jumlah
    FROM t_angsuran
    WHERE t_tglpembayaranangsuran IS NOT NULL
) AS all_realisasi
WHERE YEAR(tgl) = 2025
GROUP BY YEAR(tgl), MONTH(tgl), jenis_pajak
ORDER BY tahun, bulan, jenis_pajak;
```

## 4. Query Target vs Realisasi (Comparison)

```sql
-- Perbandingan Target vs Realisasi Per Jenis Pajak
SELECT 
    j.s_namajenis AS jenis_pajak,
    j.s_order AS urutan,
    COALESCE(SUM(td.s_targetjumlah), 0) AS target,
    COALESCE(r.total_realisasi, 0) AS realisasi,
    COALESCE(SUM(td.s_targetjumlah), 0) - COALESCE(r.total_realisasi, 0) AS selisih,
    CASE 
        WHEN SUM(td.s_targetjumlah) > 0 THEN 
            ROUND((COALESCE(r.total_realisasi, 0) / SUM(td.s_targetjumlah)) * 100, 2)
        ELSE 0 
    END AS persentase_pencapaian
FROM s_jenisobjek j
LEFT JOIN s_rekening rek ON rek.s_jenisobjek = j.s_idjenis
LEFT JOIN s_targetdetail td ON td.s_targetrekening = rek.s_idkorek
LEFT JOIN s_target t ON t.s_idtarget = td.s_idtargetheader AND t.s_tahuntarget = 2025
LEFT JOIN (
    SELECT 
        t_jenispajak,
        SUM(t_jmlhpembayaran) AS total_realisasi
    FROM t_transaksi
    WHERE YEAR(t_tglpembayaran) = 2025
    GROUP BY t_jenispajak
) r ON r.t_jenispajak = j.s_idjenis
GROUP BY j.s_idjenis, j.s_namajenis, j.s_order, r.total_realisasi
ORDER BY j.s_order;
```

## 5. Query Top Contributors

```sql
-- Top 10 Wajib Pajak dengan Realisasi Terbesar
SELECT 
    wp.t_npwpd,
    wp.t_nama AS nama_wp,
    j.s_namajenis AS jenis_pajak,
    SUM(t.t_jmlhpembayaran) AS total_pembayaran,
    COUNT(t.t_idtransaksi) AS jumlah_transaksi
FROM t_transaksi t
JOIN t_wpobjek obj ON t.t_idwpobjek = obj.t_idobjek
JOIN t_wp wp ON obj.t_idwp = wp.t_idwp
JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
WHERE YEAR(t.t_tglpembayaran) = 2025
GROUP BY wp.t_idwp, wp.t_npwpd, wp.t_nama, j.s_namajenis
ORDER BY total_pembayaran DESC
LIMIT 10;
```

## 6. Query Trend Bulanan

```sql
-- Trend Realisasi Per Bulan (Cumulative)
SELECT 
    MONTH(t_tglpembayaran) AS bulan,
    SUM(t_jmlhpembayaran) AS realisasi_bulan,
    SUM(SUM(t_jmlhpembayaran)) OVER (ORDER BY MONTH(t_tglpembayaran)) AS realisasi_kumulatif
FROM t_transaksi
WHERE YEAR(t_tglpembayaran) = 2025
GROUP BY MONTH(t_tglpembayaran)
ORDER BY bulan;
```

## 7. Query Realisasi Per Kecamatan (untuk Map Integration)

```sql
-- Realisasi Per Kecamatan (jika ada data lokasi)
SELECT 
    -- Asumsi ada field kecamatan di objek pajak atau WP
    wp.t_kecamatan AS kecamatan,
    j.s_namajenis AS jenis_pajak,
    COUNT(DISTINCT obj.t_idobjek) AS jumlah_objek,
    COUNT(t.t_idtransaksi) AS jumlah_transaksi,
    SUM(t.t_jmlhpembayaran) AS total_realisasi
FROM t_transaksi t
JOIN t_wpobjek obj ON t.t_idwpobjek = obj.t_idobjek
JOIN t_wp wp ON obj.t_idwp = wp.t_idwp
JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
WHERE YEAR(t.t_tglpembayaran) = 2025
GROUP BY wp.t_kecamatan, j.s_namajenis
ORDER BY total_realisasi DESC;
```

## 8. Query Dashboard Summary

```sql
-- Summary untuk Dashboard Utama
SELECT 
    (SELECT SUM(s_targetjumlah) FROM s_targetdetail td 
     JOIN s_target t ON td.s_idtargetheader = t.s_idtarget 
     WHERE t.s_tahuntarget = 2025) AS total_target,
    
    (SELECT SUM(t_jmlhpembayaran) FROM t_transaksi 
     WHERE YEAR(t_tglpembayaran) = 2025) AS total_realisasi,
    
    (SELECT COUNT(*) FROM t_wp) AS total_wp,
    
    (SELECT COUNT(*) FROM t_wpobjek) AS total_objek,
    
    (SELECT COUNT(*) FROM t_transaksi 
     WHERE YEAR(t_tglpembayaran) = 2025) AS total_transaksi,
    
    (SELECT COUNT(DISTINCT t_jenispajak) FROM t_transaksi 
     WHERE YEAR(t_tglpembayaran) = 2025) AS jenis_pajak_aktif;
```

---

## Notes:
- Semua query menggunakan tahun 2025 sebagai contoh
- Perlu disesuaikan dengan struktur tabel aktual di database
- Beberapa field seperti `t_kecamatan` mungkin perlu disesuaikan dengan nama kolom yang sebenarnya
- Query perlu dioptimasi dengan indexing untuk performa yang baik
