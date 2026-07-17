package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.ProyeksiIipa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyeksiIipaRepository extends JpaRepository<ProyeksiIipa, Long> {

    List<ProyeksiIipa> findByTahunOrderByJenisPajakIdAscBulanAsc(Integer tahun);

    List<ProyeksiIipa> findByTahunAndJenisPajakIdOrderByBulanAsc(Integer tahun, Integer jenisPajakId);

    @Query("SELECT p FROM ProyeksiIipa p WHERE p.tahun = :tahun ORDER BY p.bulan ASC, p.jenisPajakId ASC")
    List<ProyeksiIipa> findSummaryByTahun(@Param("tahun") Integer tahun);
}
