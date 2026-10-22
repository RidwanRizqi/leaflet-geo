package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.OfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Integer> {

    List<OfficeLocation> findByIsActiveTrue();

    Optional<OfficeLocation> findFirstByIsActiveTrue();
}
