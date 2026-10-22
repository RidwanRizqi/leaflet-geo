package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.PbjtPotensiDTO;
import com.example.leaflet_geo.dto.PbjtPotensiObservationDTO;
import com.example.leaflet_geo.entity.PbjtPotensi;
import com.example.leaflet_geo.entity.PbjtPotensiObservation;
import com.example.leaflet_geo.repository.pbjt.PbjtPotensiRepository;
import com.example.leaflet_geo.repository.pbjt.PbjtPotensiObservationRepository;
import com.example.leaflet_geo.entity.PbjtAssessment;
import com.example.leaflet_geo.repository.pbjt.PbjtAssessmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PbjtPotensiService {

    private final PbjtPotensiRepository potensiRepository;
    private final PbjtPotensiObservationRepository observationRepository;
    private final PbjtMasterMenuService masterMenuService;
    private final PbjtAssessmentRepository assessmentRepository;

    public PbjtPotensiService(PbjtPotensiRepository potensiRepository,
                              PbjtPotensiObservationRepository observationRepository,
                              PbjtMasterMenuService masterMenuService,
                              PbjtAssessmentRepository assessmentRepository) {
        this.potensiRepository = potensiRepository;
        this.observationRepository = observationRepository;
        this.masterMenuService = masterMenuService;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public String generateKodePotensi(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        String prefix = "POT-" + targetDate.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = potensiRepository.countByKodePotensiPrefix(prefix) + 1;
        String candidate;
        do {
            candidate = String.format("%s%04d", prefix, count);
            count++;
        } while (potensiRepository.existsByKodePotensi(candidate));
        return candidate;
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public PbjtPotensiDTO createPotensi(PbjtPotensiDTO dto, String username) {
        if (dto.getNamaUsaha() == null || dto.getNamaUsaha().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama usaha wajib diisi");
        }

        LocalDate tanggal = dto.getTanggalPendataan() != null ? dto.getTanggalPendataan() : LocalDate.now();
        String kode = (dto.getKodePotensi() != null && !dto.getKodePotensi().trim().isEmpty())
                ? dto.getKodePotensi().trim()
                : generateKodePotensi(tanggal);

        // Auto-sync menu items ke catalog master menu
        if (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty()) {
            masterMenuService.syncMenuItems(dto.getMenuItems());
        }

        // Hitung estimasi omzet dan pajak
        calculateRevenueAndTax(dto);

        PbjtPotensi entity = toEntity(dto);
        entity.setKodePotensi(kode);
        entity.setTanggalPendataan(tanggal);
        entity.setSurveyorId(username != null && !username.trim().isEmpty()
                ? username.trim()
                : dto.getSurveyorId());
        entity.setUpdatedBy(username != null ? username : "system");

        PbjtPotensi saved = potensiRepository.save(entity);
        log.info("PbjtPotensi created with kode: {}", saved.getKodePotensi());
        return toDTO(saved);
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public PbjtPotensiDTO updatePotensi(Long id, PbjtPotensiDTO dto, String username) {
        PbjtPotensi existing = potensiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data Potensi dengan ID " + id + " tidak ditemukan"));

        if (dto.getNamaUsaha() == null || dto.getNamaUsaha().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama usaha wajib diisi");
        }

        // Auto-sync menu items ke catalog master menu
        if (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty()) {
            masterMenuService.syncMenuItems(dto.getMenuItems());
        }

        // Hitung ulang estimasi omzet dan pajak
        calculateRevenueAndTax(dto);

        existing.setNamaUsaha(dto.getNamaUsaha().trim());
        existing.setTipeUsaha(dto.getTipeUsaha());
        if (dto.getTanggalPendataan() != null) {
            existing.setTanggalPendataan(dto.getTanggalPendataan());
        }
        existing.setKapasitasKursi(dto.getKapasitasKursi());
        existing.setLuasBangunan(dto.getLuasBangunan());
        existing.setJamBuka(dto.getJamBuka());
        existing.setJamTutup(dto.getJamTutup());
        existing.setPaymentMethods(dto.getPaymentMethods());
        existing.setAlamat(dto.getAlamat());
        existing.setKelurahan(dto.getKelurahan());
        existing.setKecamatan(dto.getKecamatan());
        existing.setKabupaten(dto.getKabupaten() != null ? dto.getKabupaten() : "Lumajang");
        existing.setKdKec(dto.getKdKec());
        existing.setKdKel(dto.getKdKel());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());
        existing.setRoadType(dto.getRoadType());
        existing.setNearSchool(dto.getNearSchool());
        existing.setNearOffice(dto.getNearOffice());
        existing.setNearMarket(dto.getNearMarket());
        existing.setCrowdLevel(dto.getCrowdLevel());
        existing.setOpeningDaysPerMonth(dto.getOpeningDaysPerMonth());
        existing.setMenuItems(dto.getMenuItems());
        existing.setCustomerOrderEstimates(dto.getCustomerOrderEstimates());
        existing.setBestSellingMenus(dto.getBestSellingMenus());
        existing.setCrowdPeakTimes(dto.getCrowdPeakTimes());
        existing.setOmzetBulanan(dto.getOmzetBulanan());
        existing.setPbjtBulanan(dto.getPbjtBulanan());
        existing.setPbjtTahunan(dto.getPbjtTahunan());
        existing.setSurveyorId(dto.getSurveyorId());
        existing.setUpdatedBy(username != null ? username : "system");
        existing.setPhotoUrls(dto.getPhotoUrls());
        existing.setSupportingDocUrl(dto.getSupportingDocUrl());
        existing.setNotes(dto.getNotes());

        PbjtPotensi updated = potensiRepository.save(existing);
        log.info("PbjtPotensi updated with id: {}", updated.getId());
        return toDTO(updated);
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public PbjtPotensiDTO getPotensiById(Long id) {
        return potensiRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Data Potensi dengan ID " + id + " tidak ditemukan"));
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public Page<PbjtPotensiDTO> searchPotensi(String search, int page, int size) {
        return searchPotensi(search, null, page, size);
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public Page<PbjtPotensiDTO> searchPotensi(String search, String surveyorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (surveyorId != null && !surveyorId.trim().isEmpty()) {
            return potensiRepository.searchPotensiWithSurveyor(search, surveyorId.trim(), pageable).map(this::toDTO);
        }
        return potensiRepository.searchPotensi(search, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public List<PbjtPotensiDTO> getPotensiForExport(LocalDate startDate, LocalDate endDate, String surveyorId) {
        List<PbjtPotensi> list;
        boolean hasSurveyor = surveyorId != null && !surveyorId.trim().isEmpty();

        if (startDate != null && endDate != null) {
            if (hasSurveyor) {
                list = potensiRepository.findBySurveyorIdAndTanggalPendataanBetweenOrderByTanggalPendataanDesc(surveyorId.trim(), startDate, endDate);
            } else {
                list = potensiRepository.findByTanggalPendataanBetweenOrderByTanggalPendataanDesc(startDate, endDate);
            }
        } else {
            if (hasSurveyor) {
                list = potensiRepository.findBySurveyorIdOrderByTanggalPendataanDesc(surveyorId.trim());
            } else {
                list = potensiRepository.findAllByOrderByTanggalPendataanDesc();
            }
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public void deletePotensi(Long id) {
        if (!potensiRepository.existsById(id)) {
            throw new IllegalArgumentException("Data Potensi tidak ditemukan");
        }
        potensiRepository.deleteById(id);
        log.info("PbjtPotensi deleted with id: {}", id);
    }

    /**
     * Hitung estimasi omzet dan pajak berbasis metode menu & faktor lokasi/kapasitas
     */
    public void calculateRevenueAndTax(PbjtPotensiDTO dto) {
        if (dto.getMenuItems() == null || dto.getMenuItems().isEmpty()) {
            dto.setOmzetBulanan(BigDecimal.ZERO);
            dto.setPbjtBulanan(BigDecimal.ZERO);
            dto.setPbjtTahunan(BigDecimal.ZERO);
            return;
        }

        List<BigDecimal> foodPrices = new ArrayList<>();
        List<BigDecimal> bevPrices = new ArrayList<>();

        for (Map<String, Object> item : dto.getMenuItems()) {
            String cat = "";
            if (item.get("category") != null) cat = item.get("category").toString().toUpperCase();
            else if (item.get("kategori") != null) cat = item.get("kategori").toString().toUpperCase();

            BigDecimal price = BigDecimal.ZERO;
            Object priceObj = item.get("price") != null ? item.get("price") : item.get("harga");
            if (priceObj != null) {
                try {
                    price = new BigDecimal(priceObj.toString());
                } catch (Exception ignored) {}
            }

            if (price.compareTo(BigDecimal.ZERO) > 0) {
                if ("BEVERAGE".equalsIgnoreCase(cat)) {
                    bevPrices.add(price);
                } else {
                    foodPrices.add(price);
                }
            }
        }

        BigDecimal avgFood = calculateAverage(foodPrices);
        BigDecimal avgBev = calculateAverage(bevPrices);
        BigDecimal spendPerPerson = avgFood.add(avgBev);

        if (spendPerPerson.compareTo(BigDecimal.ZERO) == 0) {
            dto.setOmzetBulanan(BigDecimal.ZERO);
            dto.setPbjtBulanan(BigDecimal.ZERO);
            dto.setPbjtTahunan(BigDecimal.ZERO);
            return;
        }

        // Estimasi pengunjung harian
        int capacity = (dto.getKapasitasKursi() != null && dto.getKapasitasKursi() > 0) ? dto.getKapasitasKursi() : 30;
        double turnoverRate = 2.0;

        LocalTime start = dto.getJamBuka() != null ? dto.getJamBuka() : LocalTime.of(8, 0);
        LocalTime end = dto.getJamTutup() != null ? dto.getJamTutup() : LocalTime.of(22, 0);
        double operatingHours = Math.max(1.0, Duration.between(start, end).toMinutes() / 60.0);
        double hoursFactor = Math.max(0.5, operatingHours / 8.0);

        BigDecimal locationScore = calculateLocationScore(dto);
        BigDecimal typeCoeff = getTypeCoefficient(dto.getTipeUsaha(), capacity);

        double crowdMultiplier = 1.0;
        if ("SEPI".equalsIgnoreCase(dto.getCrowdLevel())) crowdMultiplier = 0.5;
        else if ("RAMAI".equalsIgnoreCase(dto.getCrowdLevel())) crowdMultiplier = 1.5;

        double estimatedDailyVisitors = capacity * turnoverRate * hoursFactor * locationScore.doubleValue() * typeCoeff.doubleValue() * crowdMultiplier;
        BigDecimal dailyVisitors = BigDecimal.valueOf(estimatedDailyVisitors).setScale(0, RoundingMode.HALF_UP);

        int openingDays = (dto.getOpeningDaysPerMonth() != null && dto.getOpeningDaysPerMonth() > 0)
                ? dto.getOpeningDaysPerMonth() : 30;

        BigDecimal monthlyRevenue = spendPerPerson
                .multiply(dailyVisitors)
                .multiply(BigDecimal.valueOf(openingDays))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyTax = monthlyRevenue.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal annualTax = monthlyTax.multiply(BigDecimal.valueOf(12)).multiply(new BigDecimal("1.03")).setScale(2, RoundingMode.HALF_UP);

        dto.setOmzetBulanan(monthlyRevenue);
        dto.setPbjtBulanan(monthlyTax);
        dto.setPbjtTahunan(annualTax);
    }

    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLocationScore(PbjtPotensiDTO dto) {
        BigDecimal score = BigDecimal.ONE;
        if (Boolean.TRUE.equals(dto.getNearSchool())) score = score.add(new BigDecimal("0.10"));
        if (Boolean.TRUE.equals(dto.getNearOffice())) score = score.add(new BigDecimal("0.15"));
        if (Boolean.TRUE.equals(dto.getNearMarket())) score = score.add(new BigDecimal("0.12"));

        if (dto.getRoadType() != null) {
            String road = dto.getRoadType().toUpperCase();
            if (road.contains("ARTERI")) score = score.add(new BigDecimal("0.20"));
            else if (road.contains("KOLEKTOR")) score = score.add(new BigDecimal("0.15"));
            else if (road.contains("LOKAL")) score = score.add(new BigDecimal("0.05"));
        }

        if (score.compareTo(new BigDecimal("1.50")) > 0) score = new BigDecimal("1.50");
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getTypeCoefficient(String businessType, Integer seatingCapacity) {
        if (businessType != null) {
            switch (businessType.toUpperCase()) {
                case "WARUNG_KECIL": return new BigDecimal("0.85");
                case "RUMAH_MAKAN": return new BigDecimal("1.00");
                case "RESTAURANT": return new BigDecimal("1.10");
                case "CAFE_MODERN": return new BigDecimal("1.20");
                case "FRANCHISE": return new BigDecimal("1.15");
            }
        }
        if (seatingCapacity != null) {
            if (seatingCapacity < 20) return new BigDecimal("0.85");
            if (seatingCapacity < 50) return new BigDecimal("1.00");
            if (seatingCapacity < 150) return new BigDecimal("1.10");
            return new BigDecimal("1.20");
        }
        return BigDecimal.ONE;
    }

    public PbjtPotensiDTO toDTO(PbjtPotensi entity) {
        return PbjtPotensiDTO.builder()
                .id(entity.getId())
                .kodePotensi(entity.getKodePotensi())
                .namaUsaha(entity.getNamaUsaha())
                .tipeUsaha(entity.getTipeUsaha())
                .tanggalPendataan(entity.getTanggalPendataan())
                .kapasitasKursi(entity.getKapasitasKursi())
                .luasBangunan(entity.getLuasBangunan())
                .jamBuka(entity.getJamBuka())
                .jamTutup(entity.getJamTutup())
                .paymentMethods(entity.getPaymentMethods())
                .alamat(entity.getAlamat())
                .kelurahan(entity.getKelurahan())
                .kecamatan(entity.getKecamatan())
                .kabupaten(entity.getKabupaten())
                .kdKec(entity.getKdKec())
                .kdKel(entity.getKdKel())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .roadType(entity.getRoadType())
                .nearSchool(entity.getNearSchool())
                .nearOffice(entity.getNearOffice())
                .nearMarket(entity.getNearMarket())
                .crowdLevel(entity.getCrowdLevel())
                .openingDaysPerMonth(entity.getOpeningDaysPerMonth())
                .menuItems(entity.getMenuItems())
                .customerOrderEstimates(entity.getCustomerOrderEstimates())
                .bestSellingMenus(entity.getBestSellingMenus())
                .crowdPeakTimes(entity.getCrowdPeakTimes())
                .omzetBulanan(entity.getOmzetBulanan())
                .pbjtBulanan(entity.getPbjtBulanan())
                .pbjtTahunan(entity.getPbjtTahunan())
                .surveyorId(entity.getSurveyorId())
                .updatedBy(entity.getUpdatedBy())
                .photoUrls(entity.getPhotoUrls())
                .supportingDocUrl(entity.getSupportingDocUrl())
                .notes(entity.getNotes())
                .observationCount(entity.getObservations() != null ? entity.getObservations().size() : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PbjtPotensi toEntity(PbjtPotensiDTO dto) {
        return PbjtPotensi.builder()
                .id(dto.getId())
                .kodePotensi(dto.getKodePotensi())
                .namaUsaha(dto.getNamaUsaha())
                .tipeUsaha(dto.getTipeUsaha())
                .tanggalPendataan(dto.getTanggalPendataan())
                .kapasitasKursi(dto.getKapasitasKursi())
                .luasBangunan(dto.getLuasBangunan())
                .jamBuka(dto.getJamBuka())
                .jamTutup(dto.getJamTutup())
                .paymentMethods(dto.getPaymentMethods())
                .alamat(dto.getAlamat())
                .kelurahan(dto.getKelurahan())
                .kecamatan(dto.getKecamatan())
                .kabupaten(dto.getKabupaten())
                .kdKec(dto.getKdKec())
                .kdKel(dto.getKdKel())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .roadType(dto.getRoadType())
                .nearSchool(dto.getNearSchool())
                .nearOffice(dto.getNearOffice())
                .nearMarket(dto.getNearMarket())
                .crowdLevel(dto.getCrowdLevel())
                .openingDaysPerMonth(dto.getOpeningDaysPerMonth())
                .menuItems(dto.getMenuItems())
                .customerOrderEstimates(dto.getCustomerOrderEstimates())
                .bestSellingMenus(dto.getBestSellingMenus())
                .crowdPeakTimes(dto.getCrowdPeakTimes())
                .omzetBulanan(dto.getOmzetBulanan())
                .pbjtBulanan(dto.getPbjtBulanan())
                .pbjtTahunan(dto.getPbjtTahunan())
                .surveyorId(dto.getSurveyorId())
                .updatedBy(dto.getUpdatedBy())
                .photoUrls(dto.getPhotoUrls())
                .supportingDocUrl(dto.getSupportingDocUrl())
                .notes(dto.getNotes())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMenusByBusinessName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Map.of("success", true, "found", false, "menuItems", List.of());
        }
        String cleanName = name.trim();

        // 1. Cek di pbjt_potensi terlebih dahulu (nama persis, case-insensitive)
        List<PbjtPotensi> listPotensi = potensiRepository.findByNamaUsahaIgnoreCase(cleanName);
        for (PbjtPotensi p : listPotensi) {
            if (p.getMenuItems() != null && !p.getMenuItems().isEmpty()) {
                return Map.of(
                    "success", true,
                    "found", true,
                    "source", "POTENSI",
                    "businessName", p.getNamaUsaha(),
                    "menuItems", p.getMenuItems()
                );
            }
        }

        // 2. Cek di pbjt_assessments (nama persis, case-insensitive)
        List<PbjtAssessment> listAssessment = assessmentRepository.findByBusinessNameIgnoreCase(cleanName);
        for (PbjtAssessment a : listAssessment) {
            if (a.getMenuItems() != null && !a.getMenuItems().isEmpty()) {
                return Map.of(
                    "success", true,
                    "found", true,
                    "source", "ASSESSMENT",
                    "businessName", a.getBusinessName(),
                    "menuItems", a.getMenuItems()
                );
            }
        }

        // 3. Cek pencocokan parsial / like jika nama usaha mengandung kata kunci yang dicari
        List<PbjtPotensi> likePotensi = potensiRepository.findAll().stream()
            .filter(p -> p.getNamaUsaha() != null && p.getMenuItems() != null && !p.getMenuItems().isEmpty() &&
                    (p.getNamaUsaha().equalsIgnoreCase(cleanName) ||
                     p.getNamaUsaha().toLowerCase().contains(cleanName.toLowerCase()) ||
                     cleanName.toLowerCase().contains(p.getNamaUsaha().toLowerCase())))
            .collect(Collectors.toList());
        if (!likePotensi.isEmpty()) {
            PbjtPotensi p = likePotensi.get(0);
            return Map.of(
                "success", true,
                "found", true,
                "source", "POTENSI",
                "businessName", p.getNamaUsaha(),
                "menuItems", p.getMenuItems()
            );
        }

        List<PbjtAssessment> likeAssessment = assessmentRepository.findAll().stream()
            .filter(a -> a.getBusinessName() != null && a.getMenuItems() != null && !a.getMenuItems().isEmpty() &&
                    (a.getBusinessName().equalsIgnoreCase(cleanName) ||
                     a.getBusinessName().toLowerCase().contains(cleanName.toLowerCase()) ||
                     cleanName.toLowerCase().contains(a.getBusinessName().toLowerCase())))
            .collect(Collectors.toList());
        if (!likeAssessment.isEmpty()) {
            PbjtAssessment a = likeAssessment.get(0);
            return Map.of(
                "success", true,
                "found", true,
                "source", "ASSESSMENT",
                "businessName", a.getBusinessName(),
                "menuItems", a.getMenuItems()
            );
        }

        return Map.of("success", true, "found", false, "menuItems", List.of());
    }

    @Transactional(readOnly = true)
    public List<String> getAllBusinessNames() {
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(potensiRepository.findDistinctNamaUsaha());
        set.addAll(assessmentRepository.findDistinctBusinessNames());
        return new ArrayList<>(set);
    }

    // ================= FITUR OBSERVASI POTENSI =================

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public List<PbjtPotensiObservationDTO> getObservations(Long potensiId) {
        List<PbjtPotensiObservation> list = observationRepository.findByPotensiIdOrderByTanggalObservasiAsc(potensiId);
        List<PbjtPotensiObservationDTO> dtos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            PbjtPotensiObservationDTO dto = toObservationDTO(list.get(i));
            dto.setSurveyKe(i + 1);
            dtos.add(dto);
        }
        java.util.Collections.reverse(dtos);
        return dtos;
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public PbjtPotensiObservationDTO addObservation(Long potensiId, PbjtPotensiObservationDTO dto, String username) {
        PbjtPotensi potensi = potensiRepository.findById(potensiId)
                .orElseThrow(() -> new IllegalArgumentException("Data Potensi dengan ID " + potensiId + " tidak ditemukan"));

        if (dto.getJumlahPengunjung() == null || dto.getJumlahPengunjung() <= 0) {
            throw new IllegalArgumentException("Jumlah pengunjung wajib diisi minimal 1 orang");
        }

        BigDecimal durasi = (dto.getDurasiJam() != null && dto.getDurasiJam().compareTo(BigDecimal.ZERO) > 0)
                ? dto.getDurasiJam() : BigDecimal.ONE;

        BigDecimal vph = BigDecimal.valueOf(dto.getJumlahPengunjung()).divide(durasi, 2, RoundingMode.HALF_UP);

        // Menu items for observation
        List<Map<String, Object>> obsMenuItems = (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty())
                ? dto.getMenuItems()
                : (potensi.getMenuItems() != null ? potensi.getMenuItems() : new ArrayList<>());

        if (dto.getMenuItems() != null && !dto.getMenuItems().isEmpty()) {
            masterMenuService.syncMenuItems(dto.getMenuItems());
            mergeMenuItemsToPotensi(potensi, dto.getMenuItems());
        }

        // Hitung rata-rata harga makanan dan minuman dari menu observasi
        List<BigDecimal> foodPrices = new ArrayList<>();
        List<BigDecimal> bevPrices = new ArrayList<>();
        for (Map<String, Object> item : obsMenuItems) {
            String cat = "";
            if (item.get("category") != null) cat = item.get("category").toString().toUpperCase();
            else if (item.get("kategori") != null) cat = item.get("kategori").toString().toUpperCase();

            BigDecimal price = BigDecimal.ZERO;
            Object priceObj = item.get("price") != null ? item.get("price") : item.get("harga");
            if (priceObj != null) {
                try {
                    price = new BigDecimal(priceObj.toString());
                } catch (Exception ignored) {}
            }
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                if ("BEVERAGE".equalsIgnoreCase(cat)) {
                    bevPrices.add(price);
                } else {
                    foodPrices.add(price);
                }
            }
        }

        BigDecimal avgFood = calculateAverage(foodPrices);
        BigDecimal avgBev = calculateAverage(bevPrices);
        BigDecimal spendPerPerson = avgFood.add(avgBev);

        // Operating hours calculation
        LocalTime start = potensi.getJamBuka() != null ? potensi.getJamBuka() : LocalTime.of(8, 0);
        LocalTime end = potensi.getJamTutup() != null ? potensi.getJamTutup() : LocalTime.of(22, 0);
        double operatingHours = Math.max(1.0, Duration.between(start, end).toMinutes() / 60.0);

        BigDecimal estDailyVisitors = vph.multiply(BigDecimal.valueOf(operatingHours)).setScale(0, RoundingMode.HALF_UP);
        int openingDays = (potensi.getOpeningDaysPerMonth() != null && potensi.getOpeningDaysPerMonth() > 0)
                ? potensi.getOpeningDaysPerMonth() : 30;

        BigDecimal estMonthlyRevenue = spendPerPerson
                .multiply(estDailyVisitors)
                .multiply(BigDecimal.valueOf(openingDays))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal estMonthlyPbjt = estMonthlyRevenue.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estAnnualPbjt = estMonthlyPbjt.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);

        PbjtPotensiObservation obs = new PbjtPotensiObservation();
        obs.setPotensi(potensi);
        obs.setTanggalObservasi(dto.getTanggalObservasi() != null ? dto.getTanggalObservasi() : LocalDateTime.now());
        obs.setTipeWaktu(dto.getTipeWaktu() != null ? dto.getTipeWaktu() : "PEAK");
        obs.setJumlahPengunjung(dto.getJumlahPengunjung());
        obs.setDurasiJam(durasi);
        obs.setPengunjungPerJam(vph);
        obs.setMenuItems(obsMenuItems);
        obs.setAvgFoodPrice(avgFood);
        obs.setAvgBeveragePrice(avgBev);
        obs.setSpendPerPerson(spendPerPerson);
        obs.setEstimatedDailyVisitors(estDailyVisitors);
        obs.setEstimatedMonthlyRevenue(estMonthlyRevenue);
        obs.setEstimatedMonthlyPbjt(estMonthlyPbjt);
        obs.setEstimatedAnnualPbjt(estAnnualPbjt);
        obs.setPetugas((dto.getPetugas() != null && !dto.getPetugas().trim().isEmpty()) ? dto.getPetugas() : username);
        obs.setCatatan(dto.getCatatan());
        obs.setPhotoUrls(dto.getPhotoUrls());
        obs.setCreatedAt(LocalDateTime.now());

        PbjtPotensiObservation savedObs = observationRepository.save(obs);

        // Update omzet dan potensi pajak parent Potensi dengan data observasi terbaru
        recalculatePotensiFromObservations(potensi);
        potensiRepository.save(potensi);

        log.info("Observasi potensi berhasil disimpan. ID: {}, Potensi: {}, VPH: {}, Estimasi Omzet: {}",
                savedObs.getId(), potensi.getNamaUsaha(), vph, estMonthlyRevenue);

        PbjtPotensiObservationDTO resultDto = toObservationDTO(savedObs);
        List<PbjtPotensiObservation> all = observationRepository.findByPotensiIdOrderByTanggalObservasiAsc(potensiId);
        resultDto.setSurveyKe(all.size());
        return resultDto;
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public void deleteObservation(Long obsId) {
        PbjtPotensiObservation obs = observationRepository.findById(obsId)
                .orElseThrow(() -> new IllegalArgumentException("Observasi dengan ID " + obsId + " tidak ditemukan"));
        PbjtPotensi potensi = obs.getPotensi();
        observationRepository.delete(obs);

        // Recalculate parent potensi
        recalculatePotensiFromObservations(potensi);
        potensiRepository.save(potensi);
        log.info("Observasi {} dihapus dari Potensi {}", obsId, potensi.getId());
    }

    private void recalculatePotensiFromObservations(PbjtPotensi potensi) {
        List<PbjtPotensiObservation> allObs = observationRepository.findByPotensiIdOrderByTanggalObservasiDesc(potensi.getId());
        if (allObs == null || allObs.isEmpty()) {
            // Jika tidak ada observasi lagi, kembalikan ke perhitungan kapasitas standar
            PbjtPotensiDTO tempDto = toDTO(potensi);
            calculateRevenueAndTax(tempDto);
            potensi.setOmzetBulanan(tempDto.getOmzetBulanan());
            potensi.setPbjtBulanan(tempDto.getPbjtBulanan());
            potensi.setPbjtTahunan(tempDto.getPbjtTahunan());
            return;
        }

        // Parent Potensi merefleksikan observasi TERBARU (Latest Active Survey)
        PbjtPotensiObservation latestObs = allObs.get(0);
        potensi.setOmzetBulanan(latestObs.getEstimatedMonthlyRevenue());
        potensi.setPbjtBulanan(latestObs.getEstimatedMonthlyPbjt());
        potensi.setPbjtTahunan(latestObs.getEstimatedAnnualPbjt());
        if (latestObs.getPhotoUrls() != null && latestObs.getPhotoUrls().length > 0) {
            potensi.setPhotoUrls(latestObs.getPhotoUrls());
        }
    }

    private void mergeMenuItemsToPotensi(PbjtPotensi potensi, List<Map<String, Object>> newItems) {
        List<Map<String, Object>> current = potensi.getMenuItems();
        if (current == null) {
            current = new ArrayList<>();
        }
        Set<String> existingNames = current.stream()
                .filter(m -> m.get("name") != null || m.get("nama") != null)
                .map(m -> m.get("name") != null ? m.get("name").toString().trim().toLowerCase() : m.get("nama").toString().trim().toLowerCase())
                .collect(Collectors.toSet());

        boolean changed = false;
        for (Map<String, Object> item : newItems) {
            String name = item.get("name") != null ? item.get("name").toString().trim() : (item.get("nama") != null ? item.get("nama").toString().trim() : "");
            if (!name.isEmpty() && !existingNames.contains(name.toLowerCase())) {
                current.add(item);
                existingNames.add(name.toLowerCase());
                changed = true;
            }
        }
        if (changed) {
            potensi.setMenuItems(current);
        }
    }

    private PbjtPotensiObservationDTO toObservationDTO(PbjtPotensiObservation obs) {
        PbjtPotensiObservationDTO dto = new PbjtPotensiObservationDTO();
        dto.setId(obs.getId());
        dto.setPotensiId(obs.getPotensi() != null ? obs.getPotensi().getId() : null);
        dto.setTanggalObservasi(obs.getTanggalObservasi());
        dto.setTipeWaktu(obs.getTipeWaktu());
        dto.setJumlahPengunjung(obs.getJumlahPengunjung());
        dto.setDurasiJam(obs.getDurasiJam());
        dto.setPengunjungPerJam(obs.getPengunjungPerJam());
        dto.setMenuItems(obs.getMenuItems());
        dto.setAvgFoodPrice(obs.getAvgFoodPrice());
        dto.setAvgBeveragePrice(obs.getAvgBeveragePrice());
        dto.setSpendPerPerson(obs.getSpendPerPerson());
        dto.setEstimatedDailyVisitors(obs.getEstimatedDailyVisitors());
        dto.setEstimatedMonthlyRevenue(obs.getEstimatedMonthlyRevenue());
        dto.setEstimatedMonthlyPbjt(obs.getEstimatedMonthlyPbjt());
        dto.setEstimatedAnnualPbjt(obs.getEstimatedAnnualPbjt());
        dto.setPetugas(obs.getPetugas());
        dto.setCatatan(obs.getCatatan());
        dto.setPhotoUrls(obs.getPhotoUrls());
        dto.setCreatedAt(obs.getCreatedAt());
        return dto;
    }
}
