package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.PbjtPotensiObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbjtPotensiObservationRepository extends JpaRepository<PbjtPotensiObservation, Long> {
    List<PbjtPotensiObservation> findByPotensiIdOrderByTanggalObservasiDesc(Long potensiId);
    List<PbjtPotensiObservation> findByPotensiIdOrderByTanggalObservasiAsc(Long potensiId);
    void deleteByPotensiId(Long potensiId);
}
