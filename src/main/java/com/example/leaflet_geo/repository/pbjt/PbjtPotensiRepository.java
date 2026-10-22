package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.PbjtPotensi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PbjtPotensiRepository extends JpaRepository<PbjtPotensi, Long> {

    Optional<PbjtPotensi> findByKodePotensi(String kodePotensi);

    boolean existsByKodePotensi(String kodePotensi);

    @Query("SELECT p FROM PbjtPotensi p WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.namaUsaha) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.kodePotensi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.alamat) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.kelurahan) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.kecamatan) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.tipeUsaha) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<PbjtPotensi> searchPotensi(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM PbjtPotensi p WHERE " +
           "(:surveyorId IS NULL OR :surveyorId = '' OR p.surveyorId = :surveyorId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.namaUsaha) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.kodePotensi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(p.alamat, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(p.kelurahan, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(p.kecamatan, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(p.tipeUsaha, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<PbjtPotensi> searchPotensiWithSurveyor(
            @Param("search") String search,
            @Param("surveyorId") String surveyorId,
            Pageable pageable);

    List<PbjtPotensi> findByTanggalPendataanBetweenOrderByTanggalPendataanDesc(LocalDate startDate, LocalDate endDate);

    List<PbjtPotensi> findBySurveyorIdAndTanggalPendataanBetweenOrderByTanggalPendataanDesc(String surveyorId, LocalDate startDate, LocalDate endDate);

    List<PbjtPotensi> findAllByOrderByTanggalPendataanDesc();

    List<PbjtPotensi> findBySurveyorIdOrderByTanggalPendataanDesc(String surveyorId);

    @Query("SELECT COUNT(p) FROM PbjtPotensi p WHERE p.kodePotensi LIKE CONCAT(:prefix, '%')")
    long countByKodePotensiPrefix(@Param("prefix") String prefix);

    @Query("SELECT p FROM PbjtPotensi p WHERE LOWER(TRIM(p.namaUsaha)) = LOWER(TRIM(:namaUsaha)) ORDER BY p.id DESC")
    List<PbjtPotensi> findByNamaUsahaIgnoreCase(@Param("namaUsaha") String namaUsaha);

    @Query("SELECT DISTINCT p.namaUsaha FROM PbjtPotensi p WHERE p.namaUsaha IS NOT NULL ORDER BY p.namaUsaha ASC")
    List<String> findDistinctNamaUsaha();
}
