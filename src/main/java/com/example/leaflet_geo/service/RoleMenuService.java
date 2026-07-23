package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.RoleMenuDTO;
import com.example.leaflet_geo.dto.RoleMenuUpdateRequest;
import com.example.leaflet_geo.model.RoleMenuMapping;
import com.example.leaflet_geo.repository.RoleMenuMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleMenuService {

    @Autowired
    private RoleMenuMappingRepository repository;

    public List<RoleMenuDTO> getAllRoleMenus() {
        List<String> roleNames = repository.findDistinctRoleNameBy();

        return roleNames.stream().map(roleName -> {
            List<Integer> menuIds = repository.findMenuIdByRoleName(roleName)
                    .stream().filter(id -> id != 0).collect(Collectors.toList());
            return RoleMenuDTO.builder()
                    .roleName(roleName)
                    .menuIds(menuIds)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<Integer> getMenuIdsByRole(String roleName) {
        return repository.findMenuIdByRoleName(roleName)
                .stream().filter(id -> id != 0).collect(Collectors.toList());
    }

    public List<Integer> getMenuIdsForCurrentUser(String role) {
        if (role == null || role.isEmpty()) {
            return List.of();
        }
        return getMenuIdsByRole(role.toUpperCase());
    }

    @Transactional
    public void updateRoleMenu(RoleMenuUpdateRequest request) {
        String roleName = request.getRoleName().toUpperCase();

        // Delete existing mappings for this role
        List<RoleMenuMapping> existing = repository.findByRoleName(roleName);
        if (!existing.isEmpty()) {
            repository.deleteAll(existing);
        }

        // Insert new mappings
        LocalDateTime now = LocalDateTime.now();
        List<Integer> menuIdsToSave = request.getMenuIds();
        
        // If empty, insert a dummy menuId = 0 so the role name is persisted
        if (menuIdsToSave == null || menuIdsToSave.isEmpty()) {
            menuIdsToSave = List.of(0);
        }

        List<RoleMenuMapping> newMappings = menuIdsToSave.stream()
                .map(menuId -> {
                    RoleMenuMapping mapping = new RoleMenuMapping();
                    mapping.setId(UUID.randomUUID().toString());
                    mapping.setRoleName(roleName);
                    mapping.setMenuId(menuId);
                    mapping.setCreatedAt(now);
                    mapping.setUpdatedAt(now);
                    return mapping;
                })
                .collect(Collectors.toList());

        if (!newMappings.isEmpty()) {
            repository.saveAll(newMappings);
        }
    }

    @Transactional
    public void deleteRoleMenu(String roleName) {
        List<RoleMenuMapping> existing = repository.findByRoleName(roleName.toUpperCase());
        if (!existing.isEmpty()) {
            repository.deleteAll(existing);
        }
    }

    public List<String> getAllRoles() {
        return repository.findDistinctRoleNameBy();
    }
}
