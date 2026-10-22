package com.example.leaflet_geo.repository.pbjt;

import com.example.leaflet_geo.entity.PbjtMasterMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PbjtMasterMenuRepository extends JpaRepository<PbjtMasterMenu, Long> {

    Optional<PbjtMasterMenu> findByNamaMenuIgnoreCase(String namaMenu);

    List<PbjtMasterMenu> findByNamaMenuContainingIgnoreCaseOrderByNamaMenuAsc(String query);

    List<PbjtMasterMenu> findAllByOrderByNamaMenuAsc();

    boolean existsByNamaMenuIgnoreCase(String namaMenu);
}
