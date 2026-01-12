package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.PbjtAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PbjtAssessmentRepository extends JpaRepository<PbjtAssessment, Long> {
    
    Optional<PbjtAssessment> findByBusinessId(String businessId);
    
    List<PbjtAssessment> findByKabupaten(String kabupaten);
    
    List<PbjtAssessment> findByKecamatan(String kecamatan);
    
    List<PbjtAssessment> findByKelurahan(String kelurahan);
    
    List<PbjtAssessment> findByBusinessType(String businessType);
    
    List<PbjtAssessment> findByConfidenceLevel(String confidenceLevel);
    
    List<PbjtAssessment> findByAssessmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM PbjtAssessment a WHERE " +
           "a.latitude BETWEEN :minLat AND :maxLat AND " +
           "a.longitude BETWEEN :minLon AND :maxLon")
    List<PbjtAssessment> findByLocationBounds(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon,
        @Param("maxLon") Double maxLon
    );
    
    @Query("SELECT COUNT(a) FROM PbjtAssessment a WHERE a.kabupaten = :kabupaten")
    Long countByKabupaten(@Param("kabupaten") String kabupaten);
    
    @Query("SELECT SUM(a.annualPbjt) FROM PbjtAssessment a WHERE a.kabupaten = :kabupaten")
    Double sumAnnualPbjtByKabupaten(@Param("kabupaten") String kabupaten);
    
    boolean existsByBusinessId(String businessId);
    
    // Map statistics queries
    List<PbjtAssessment> findByKecamatanAndKelurahan(String kecamatan, String kelurahan);
    
    List<PbjtAssessment> findByKecamatanIgnoreCaseAndKelurahanIgnoreCase(String kecamatan, String kelurahan);
    
    @Query("SELECT DISTINCT a.kecamatan FROM PbjtAssessment a WHERE a.kecamatan IS NOT NULL ORDER BY a.kecamatan")
    List<String> findDistinctKecamatan();
    
    @Query("SELECT DISTINCT a.kelurahan FROM PbjtAssessment a WHERE a.kecamatan = :kecamatan AND a.kelurahan IS NOT NULL ORDER BY a.kelurahan")
    List<String> findDistinctKelurahanByKecamatan(@Param("kecamatan") String kecamatan);
}
