package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.MasterMenuDTO;
import com.example.leaflet_geo.entity.PbjtMasterMenu;
import com.example.leaflet_geo.repository.pbjt.PbjtMasterMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PbjtMasterMenuService {

    private final PbjtMasterMenuRepository masterMenuRepository;

    public PbjtMasterMenuService(PbjtMasterMenuRepository masterMenuRepository) {
        this.masterMenuRepository = masterMenuRepository;
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public List<MasterMenuDTO> getAllMenus() {
        return masterMenuRepository.findAllByOrderByNamaMenuAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "pbjtTransactionManager")
    public List<MasterMenuDTO> searchMenus(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMenus();
        }
        return masterMenuRepository.findByNamaMenuContainingIgnoreCaseOrderByNamaMenuAsc(query.trim()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public MasterMenuDTO saveMenu(MasterMenuDTO dto) {
        if (dto.getNamaMenu() == null || dto.getNamaMenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama menu tidak boleh kosong");
        }
        String nama = dto.getNamaMenu().trim();
        PbjtMasterMenu entity = masterMenuRepository.findByNamaMenuIgnoreCase(nama)
                .orElse(PbjtMasterMenu.builder()
                        .namaMenu(nama)
                        .kategori(dto.getKategori() != null ? dto.getKategori().toUpperCase() : "FOOD")
                        .defaultHarga(dto.getDefaultHarga() != null ? dto.getDefaultHarga() : BigDecimal.ZERO)
                        .build());

        if (dto.getKategori() != null) {
            entity.setKategori(dto.getKategori().toUpperCase());
        }
        if (dto.getDefaultHarga() != null && dto.getDefaultHarga().compareTo(BigDecimal.ZERO) > 0) {
            entity.setDefaultHarga(dto.getDefaultHarga());
        }

        PbjtMasterMenu saved = masterMenuRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public void syncMenuItems(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : items) {
            Object nameObj = item.get("name");
            if (nameObj == null) {
                nameObj = item.get("nama");
            }
            if (nameObj == null || nameObj.toString().trim().isEmpty()) {
                continue;
            }

            String namaMenu = nameObj.toString().trim();
            if (!masterMenuRepository.existsByNamaMenuIgnoreCase(namaMenu)) {
                String kategori = "FOOD";
                if (item.get("category") != null) {
                    kategori = item.get("category").toString().toUpperCase();
                } else if (item.get("kategori") != null) {
                    kategori = item.get("kategori").toString().toUpperCase();
                }

                BigDecimal harga = BigDecimal.ZERO;
                Object priceObj = item.get("price") != null ? item.get("price") : item.get("harga");
                if (priceObj != null) {
                    try {
                        harga = new BigDecimal(priceObj.toString());
                    } catch (Exception ignored) {}
                }

                PbjtMasterMenu newMenu = PbjtMasterMenu.builder()
                        .namaMenu(namaMenu)
                        .kategori(kategori)
                        .defaultHarga(harga)
                        .build();

                masterMenuRepository.save(newMenu);
                log.info("Menu baru otomatis ditambahkan ke Master Menu: {} ({})", namaMenu, kategori);
            }
        }
    }

    @Transactional(transactionManager = "pbjtTransactionManager")
    public long deleteAll() {
        long count = masterMenuRepository.count();
        masterMenuRepository.deleteAll();
        log.info("Deleted all {} master menus", count);
        return count;
    }

    public MasterMenuDTO toDTO(PbjtMasterMenu entity) {
        return MasterMenuDTO.builder()
                .id(entity.getId())
                .namaMenu(entity.getNamaMenu())
                .kategori(entity.getKategori())
                .defaultHarga(entity.getDefaultHarga())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
