package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.MenuObservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuObservationHistoryRepository extends JpaRepository<MenuObservationHistory, Long> {
    List<MenuObservationHistory> findByAssessmentIdOrderByObservationDateDesc(Long assessmentId);
}
