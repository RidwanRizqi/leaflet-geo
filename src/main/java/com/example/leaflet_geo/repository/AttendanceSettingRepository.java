package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.AttendanceSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceSettingRepository extends JpaRepository<AttendanceSetting, Integer> {

    Optional<AttendanceSetting> findByRoleNameIgnoreCase(String roleName);
}
