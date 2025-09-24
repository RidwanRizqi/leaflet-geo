package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.entity.Bidang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidangRepository extends JpaRepository<Bidang, UUID> {
    
    /**
     * Find by NOP (Nomor Objek Pajak)
     */
    Optional<Bidang> findByNop(String nop);
    
    /**
     * Find all active records
     */
    List<Bidang> findByIsActiveTrue();
    
    /**
     * Find by composite key components
     */
    Optional<Bidang> findByKdPropAndKdDati2AndKdKecAndKdKelAndKdBlokAndNoUrutAndKdJnsOp(
        String kdProp, String kdDati2, String kdKec, String kdKel, 
        String kdBlok, String noUrut, String kdJnsOp
    );
    
    /**
     * Find by province code
     */
    List<Bidang> findByKdPropAndIsActiveTrue(String kdProp);
    
    /**
     * Find by district code
     */
    List<Bidang> findByKdPropAndKdDati2AndIsActiveTrue(String kdProp, String kdDati2);
    
    /**
     * Find by sub-district code
     */
    List<Bidang> findByKdPropAndKdDati2AndKdKecAndIsActiveTrue(
        String kdProp, String kdDati2, String kdKec
    );
    
    /**
     * Find by village code
     */
    List<Bidang> findByKdPropAndKdDati2AndKdKecAndKdKelAndIsActiveTrue(
        String kdProp, String kdDati2, String kdKec, String kdKel
    );
    
    /**
     * Find by block code
     */
    List<Bidang> findByKdPropAndKdDati2AndKdKecAndKdKelAndKdBlokAndIsActiveTrue(
        String kdProp, String kdDati2, String kdKec, String kdKel, String kdBlok
    );
    
    /**
     * Custom query to find by partial NOP
     */
    @Query("SELECT b FROM Bidang b WHERE b.nop LIKE :partialNop% AND b.isActive = true")
    List<Bidang> findByPartialNop(@Param("partialNop") String partialNop);
    
    /**
     * Count active records by province
     */
    @Query("SELECT COUNT(b) FROM Bidang b WHERE b.kdProp = :kdProp AND b.isActive = true")
    Long countByProvince(@Param("kdProp") String kdProp);
    
    /**
     * Find all with geometry data for mapping
     */
    @Query("SELECT b FROM Bidang b WHERE b.isActive = true AND b.geom IS NOT NULL")
    List<Bidang> findAllWithGeometry();
    
    /**
     * Count active records by sub-district (kecamatan)
     */
    @Query("SELECT COUNT(b) FROM Bidang b WHERE b.kdProp = :kdProp AND b.kdDati2 = :kdDati2 AND b.kdKec = :kdKec AND b.isActive = true")
    Long countByKecamatan(@Param("kdProp") String kdProp, @Param("kdDati2") String kdDati2, @Param("kdKec") String kdKec);
    
    /**
     * Count active records by village (kelurahan)
     */
    @Query("SELECT COUNT(b) FROM Bidang b WHERE b.kdProp = :kdProp AND b.kdDati2 = :kdDati2 AND b.kdKec = :kdKec AND b.kdKel = :kdKel AND b.isActive = true")
    Long countByKelurahan(@Param("kdProp") String kdProp, @Param("kdDati2") String kdDati2, @Param("kdKec") String kdKec, @Param("kdKel") String kdKel);
}

