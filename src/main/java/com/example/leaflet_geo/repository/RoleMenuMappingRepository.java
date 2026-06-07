package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.RoleMenuMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuMappingRepository extends JpaRepository<RoleMenuMapping, String> {
    List<RoleMenuMapping> findByRoleName(String roleName);

    @Query("SELECT r.menuId FROM RoleMenuMapping r WHERE r.roleName = ?1")
    List<Integer> findMenuIdByRoleName(String roleName);

    void deleteByRoleName(String roleName);

    boolean existsByRoleNameAndMenuId(String roleName, Integer menuId);

    @Query("SELECT DISTINCT r.roleName FROM RoleMenuMapping r ORDER BY r.roleName")
    List<String> findDistinctRoleNameBy();
}
