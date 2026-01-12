package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.ObservationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationHistoryRepository extends JpaRepository<ObservationHistory, Long> {
    
    List<ObservationHistory> findByAssessmentId(Long assessmentId);
    
    List<ObservationHistory> findByDayType(String dayType);
    
    @Query("SELECT o FROM ObservationHistory o WHERE o.assessment.id = :assessmentId ORDER BY o.observationDate DESC")
    List<ObservationHistory> findByAssessmentIdOrderByDateDesc(@Param("assessmentId") Long assessmentId);
    
    @Query("SELECT COUNT(o) FROM ObservationHistory o WHERE o.assessment.id = :assessmentId")
    Long countByAssessmentId(@Param("assessmentId") Long assessmentId);
}
