package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.TargetRealisasiDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TaxProbabilityCalculationService {

    public void enrichWithProbability(TargetRealisasiDTO dto, Integer tahun) {
        double realisasi  = dto.getRealisasi() != null ? dto.getRealisasi().doubleValue() : 0.0;
        double target     = dto.getTarget()    != null ? dto.getTarget().doubleValue()    : 0.0;
        String jenisPajak = dto.getJenisPajak();

        // Tentukan tanggal referensi berdasarkan tahun filter
        LocalDate dataDate = LocalDate.now();
        if (tahun != null && tahun < dataDate.getYear()) {
            dataDate = LocalDate.of(tahun, 12, 31);
        } else if (tahun != null && tahun > dataDate.getYear()) {
            dataDate = LocalDate.of(tahun, 1, 1);
        }

        // 1. Variabel Waktu
        int hariTotal = dataDate.isLeapYear() ? 366 : 365;
        int hariYtd   = dataDate.getDayOfYear();
        int hariSisa  = hariTotal - hariYtd;

        // Handle edge case jika target 0
        if (target <= 0) {
            buildFallbackDTO(dto);
            return;
        }

        // 2. Kalkulasi Metrik Utama
        // Formula Excel:
        //   E = C/D          => % Realisasi = Realisasi / Target
        //   F = E / hariYtd  => Run-rate Aktual (% per hari)
        //   G = IF(hariSisa=0, 0, (1-E) / hariSisa)  => Run-rate Dibutuhkan
        //   H = F - G        => Defisit/Surplus Harian
        //   I = IF(G=0, 0, F/G) => Rasio
        double persenRealisasi      = realisasi / target;
        double runRateAktual        = (hariYtd > 0) ? (persenRealisasi / hariYtd) : 0.0;
        double runRateDibutuhkan    = (hariSisa > 0) ? ((1.0 - persenRealisasi) / hariSisa) : 0.0;
        double defisitSurplus       = runRateAktual - runRateDibutuhkan;
        double rasio                = (runRateDibutuhkan > 0) ? (runRateAktual / runRateDibutuhkan) : 0.0;

        // 3. Kalkulasi Probabilitas — Piecewise Linear (sesuai formula Excel J9)
        // Formula:
        //   I >= 1.40 => 0.98
        //   I >= 1.15 => 0.90 + (I-1.15)/0.25 * 0.07
        //   I >= 1.00 => 0.70 + (I-1.00)/0.15 * 0.20
        //   I >= 0.90 => 0.55 + (I-0.90)/0.10 * 0.15
        //   I >= 0.80 => 0.35 + (I-0.80)/0.10 * 0.20
        //   I >= 0.70 => 0.25 + (I-0.70)/0.10 * 0.10
        //   I <  0.70 => MAX(0.05, I * 0.20)
        // Override khusus PBB-P2 => 0.78 (musiman Q3-Q4)
        double probabilitas;
        if (jenisPajak != null && jenisPajak.contains("PBBP2")) {
            // Override musiman untuk PBB-P2 (jatuh tempo September, ACF12 dominan)
            probabilitas = 0.78;
        } else if (persenRealisasi >= 1.0) {
            probabilitas = 1.0; // Target sudah terpenuhi
        } else if (hariSisa <= 0) {
            probabilitas = 0.0; // Waktu habis, target belum tercapai
        } else {
            probabilitas = calculateProbabilityPiecewise(rasio);
        }

        // 4. Update DTO
        dto.setRunRateAktual(runRateAktual);
        dto.setRunRateDibutuhkan(runRateDibutuhkan);
        dto.setDefisitSurplus(defisitSurplus);
        dto.setRasio(rasio);
        dto.setProbabilitas(probabilitas);

        // 5. Klasifikasi Kategori & Warna (sesuai formula Excel K9)
        applyUiClassification(dto);
    }

    /**
     * Piecewise linear probability — replikasi persis formula Excel J9:
     *
     * =IF(I>=1.4, 0.98,
     *   IF(I>=1.15, 0.9+(I-1.15)/0.25*0.07,
     *     IF(I>=1,  0.7+(I-1.00)/0.15*0.20,
     *       IF(I>=0.9, 0.55+(I-0.9)/0.1*0.15,
     *         IF(I>=0.8, 0.35+(I-0.8)/0.1*0.20,
     *           IF(I>=0.7, 0.25+(I-0.7)/0.1*0.10,
     *             MAX(0.05, I*0.20)))))))
     */
    private double calculateProbabilityPiecewise(double rasio) {
        if (rasio >= 1.40) {
            return 0.98;
        } else if (rasio >= 1.15) {
            return 0.90 + (rasio - 1.15) / 0.25 * 0.07;
        } else if (rasio >= 1.00) {
            return 0.70 + (rasio - 1.00) / 0.15 * 0.20;
        } else if (rasio >= 0.90) {
            return 0.55 + (rasio - 0.90) / 0.10 * 0.15;
        } else if (rasio >= 0.80) {
            return 0.35 + (rasio - 0.80) / 0.10 * 0.20;
        } else if (rasio >= 0.70) {
            return 0.25 + (rasio - 0.70) / 0.10 * 0.10;
        } else {
            return Math.max(0.05, rasio * 0.20);
        }
    }

    /**
     * Klasifikasi kategori & warna — replikasi persis formula Excel K9:
     *
     * =IF(J>=0.9,"Sangat Tinggi",
     *    IF(J>=0.7,"Tinggi",
     *      IF(J>=0.5,"Sedang",
     *        IF(J>=0.3,"Rendah-Sedang",
     *          IF(J>=0.15,"Rendah","Sangat Rendah")))))
     */
    private void applyUiClassification(TargetRealisasiDTO dto) {
        double prob = dto.getProbabilitas() != null ? dto.getProbabilitas() : 0.0;

        if (prob >= 0.90) {
            dto.setKategori("Sangat Tinggi");
            dto.setWarna("#006400"); // Hijau Tua
            dto.setIsWarning(false);
        } else if (prob >= 0.70) {
            dto.setKategori("Tinggi");
            dto.setWarna("#90EE90"); // Hijau Muda
            dto.setIsWarning(false);
        } else if (prob >= 0.50) {
            dto.setKategori("Sedang");
            dto.setWarna("#FFD700"); // Kuning
            dto.setIsWarning(false);
        } else if (prob >= 0.30) {
            dto.setKategori("Rendah-Sedang");
            dto.setWarna("#FFA500"); // Oranye
            dto.setIsWarning(false);
        } else if (prob >= 0.15) {
            // Kategori "Rendah" — ada di Excel tapi sebelumnya tidak ada di Java
            dto.setKategori("Rendah");
            dto.setWarna("#FF6347"); // Tomat/Merah Muda
            dto.setIsWarning(true);
        } else {
            dto.setKategori("Sangat Rendah");
            dto.setWarna("#FF0000"); // Merah
            dto.setIsWarning(true);
        }
    }

    private void buildFallbackDTO(TargetRealisasiDTO dto) {
        dto.setRunRateAktual(0.0);
        dto.setRunRateDibutuhkan(0.0);
        dto.setDefisitSurplus(0.0);
        dto.setRasio(0.0);
        dto.setProbabilitas(0.0);
        dto.setKategori("Tidak Valid");
        dto.setWarna("#808080");
        dto.setIsWarning(true);
    }
}
