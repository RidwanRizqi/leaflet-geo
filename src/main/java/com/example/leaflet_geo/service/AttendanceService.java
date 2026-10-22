package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.attendance.*;
import com.example.leaflet_geo.model.Attendance;
import com.example.leaflet_geo.model.AttendanceSetting;
import com.example.leaflet_geo.model.OfficeLocation;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.repository.AttendanceRepository;
import com.example.leaflet_geo.repository.AttendanceSettingRepository;
import com.example.leaflet_geo.repository.OfficeLocationRepository;
import com.example.leaflet_geo.repository.UserRepository;
import com.example.leaflet_geo.util.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OfficeLocationRepository officeLocationRepository;

    @Autowired
    private AttendanceSettingRepository attendanceSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload.attendance-dir:uploads/attendance}")
    private String uploadAttendanceDir;

    /**
     * Absen Masuk (Check-In)
     */
    @Transactional
    public AttendanceResponseDTO checkIn(User currentUser, AttendanceCheckInRequest request, String clientIp) {
        LocalDate today = LocalDate.now();

        // Validasi: Cek apakah user sudah absen masuk hari ini
        Optional<Attendance> existingOpt = attendanceRepository.findByUserIdAndTanggal(currentUser.getId(), today);
        if (existingOpt.isPresent()) {
            throw new RuntimeException("Anda sudah melakukan absen masuk hari ini pada pukul " +
                    existingOpt.get().getJamMasuk().toLocalTime().toString().substring(0, 5));
        }

        // Ambil lokasi kantor aktif (default BPRD Lumajang jika belum ada)
        OfficeLocation office = officeLocationRepository.findFirstByIsActiveTrue()
                .orElse(OfficeLocation.builder()
                        .nama("Kantor BPRD Lumajang")
                        .latitude(-8.134386886649747)
                        .longitude(113.22632802023689)
                        .radiusMeter(150)
                        .build());

        // Hitung jarak ke kantor
        double distance = GeoUtils.calculateDistance(
                request.getLatitude(), request.getLongitude(),
                office.getLatitude(), office.getLongitude()
        );
        boolean isInside = distance <= office.getRadiusMeter();
        String lokasiStatus = isInside ? "DI_KANTOR" : "DI_LUAR_KANTOR";

        // Simpan foto selfie jika dikirim
        String fotoPath = null;
        if (request.getFotoBase64() != null && !request.getFotoBase64().trim().isEmpty()) {
            fotoPath = savePhoto(request.getFotoBase64(), "in_" + currentUser.getId() + "_" + System.currentTimeMillis());
        }

        Attendance attendance = Attendance.builder()
                .userId(currentUser.getId())
                .tanggal(today)
                .jamMasuk(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .lokasiStatus(lokasiStatus)
                .jarakMeter(Math.round(distance * 100.0) / 100.0)
                .gpsAccuracy(request.getGpsAccuracy())
                .fotoMasuk(fotoPath)
                .deviceInfo(request.getDeviceInfo())
                .ipAddress(clientIp)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return mapToDTO(saved, currentUser);
    }

    /**
     * Absen Pulang (Check-Out)
     */
    @Transactional
    public AttendanceResponseDTO checkOut(User currentUser, AttendanceCheckOutRequest request, String clientIp) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByUserIdAndTanggal(currentUser.getId(), today)
                .orElseThrow(() -> new RuntimeException("Anda belum melakukan absen masuk hari ini!"));

        if (attendance.getJamPulang() != null) {
            throw new RuntimeException("Anda sudah melakukan absen pulang hari ini pada pukul " +
                    attendance.getJamPulang().toLocalTime().toString().substring(0, 5));
        }

        OfficeLocation office = officeLocationRepository.findFirstByIsActiveTrue()
                .orElse(OfficeLocation.builder()
                        .nama("Kantor BPRD Lumajang")
                        .latitude(-8.134386886649747)
                        .longitude(113.22632802023689)
                        .radiusMeter(150)
                        .build());

        double distance = GeoUtils.calculateDistance(
                request.getLatitude(), request.getLongitude(),
                office.getLatitude(), office.getLongitude()
        );
        boolean isInside = distance <= office.getRadiusMeter();
        String pulangLokasiStatus = isInside ? "DI_KANTOR" : "DI_LUAR_KANTOR";

        String fotoPath = null;
        if (request.getFotoBase64() != null && !request.getFotoBase64().trim().isEmpty()) {
            fotoPath = savePhoto(request.getFotoBase64(), "out_" + currentUser.getId() + "_" + System.currentTimeMillis());
        }

        attendance.setJamPulang(LocalDateTime.now());
        attendance.setPulangLatitude(request.getLatitude());
        attendance.setPulangLongitude(request.getLongitude());
        attendance.setPulangLokasiStatus(pulangLokasiStatus);
        attendance.setPulangJarakMeter(Math.round(distance * 100.0) / 100.0);
        attendance.setPulangGpsAccuracy(request.getGpsAccuracy());
        attendance.setFotoPulang(fotoPath);
        attendance.setUpdatedAt(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);
        return mapToDTO(saved, currentUser);
    }

    /**
     * Mendapatkan status absensi hari ini milik user login
     */
    public AttendanceStatusTodayDTO getTodayStatus(User currentUser) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> attendanceOpt = attendanceRepository.findByUserIdAndTanggal(currentUser.getId(), today);

        // Cari setting berdasarkan role user
        String userRole = currentUser.getRole() != null ? currentUser.getRole() : "USER";
        AttendanceSetting setting = attendanceSettingRepository.findByRoleNameIgnoreCase(userRole)
                .orElse(AttendanceSetting.builder()
                        .roleName(userRole)
                        .isMandatory("MAGANG".equalsIgnoreCase(userRole))
                        .defaultLandingUrl("MAGANG".equalsIgnoreCase(userRole) ? "/absensi" : "/dashboard-pajak")
                        .build());

        OfficeLocation office = officeLocationRepository.findFirstByIsActiveTrue()
                .orElse(OfficeLocation.builder()
                        .nama("Kantor BPRD Lumajang")
                        .latitude(-8.134386886649747)
                        .longitude(113.22632802023689)
                        .radiusMeter(150)
                        .build());

        boolean hasCheckedIn = attendanceOpt.isPresent();
        boolean hasCheckedOut = attendanceOpt.isPresent() && attendanceOpt.get().getJamPulang() != null;

        return AttendanceStatusTodayDTO.builder()
                .hasCheckedIn(hasCheckedIn)
                .hasCheckedOut(hasCheckedOut)
                .isMandatory(Boolean.TRUE.equals(setting.getIsMandatory()))
                .defaultLandingUrl(setting.getDefaultLandingUrl())
                .attendance(attendanceOpt.map(a -> mapToDTO(a, currentUser)).orElse(null))
                .officeLocation(office)
                .setting(setting)
                .build();
    }

    /**
     * Riwayat absensi user login
     */
    public List<AttendanceResponseDTO> getMyHistory(User currentUser, LocalDate start, LocalDate end) {
        LocalDate endDate = end != null ? end : LocalDate.now();
        LocalDate startDate = start != null ? start : endDate.minusDays(30);

        List<Attendance> list = attendanceRepository.findByUserIdAndTanggalBetweenOrderByTanggalDesc(
                currentUser.getId(), startDate, endDate
        );

        return list.stream()
                .map(a -> mapToDTO(a, currentUser))
                .collect(Collectors.toList());
    }

    /**
     * List kehadiran untuk Dashboard Admin
     * Jika tanggal null, menampilkan semua riwayat data yang pernah absen
     */
    public List<AttendanceResponseDTO> getAdminList(LocalDate tanggal, String role) {
        String roleFilter = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : null;

        List<Attendance> attendances;
        if (tanggal != null) {
            if (roleFilter != null) {
                attendances = attendanceRepository.findByTanggalAndRole(tanggal, roleFilter);
            } else {
                attendances = attendanceRepository.findByTanggalOrderByCreatedAtDesc(tanggal);
            }
        } else {
            // Tampilkan SEMUA riwayat absensi
            if (roleFilter != null) {
                attendances = attendanceRepository.findByRoleOrderByTanggalDescCreatedAtDesc(roleFilter);
            } else {
                attendances = attendanceRepository.findAllByOrderByTanggalDescCreatedAtDesc();
            }
        }

        // Cache user map
        Map<String, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        return attendances.stream()
                .map(a -> mapToDTO(a, userMap.get(a.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * Summary statistik kehadiran untuk Admin
     */
    public AttendanceSummaryDTO getAdminSummary(LocalDate tanggal) {
        long totalUser = userRepository.count();
        long totalHadir;
        long totalDiKantor;
        long totalDiLuarKantor;
        long totalBelumHadir;

        if (tanggal != null) {
            totalHadir = attendanceRepository.countByTanggal(tanggal);
            totalDiKantor = attendanceRepository.countByTanggalAndLokasiStatus(tanggal, "DI_KANTOR");
            totalDiLuarKantor = attendanceRepository.countByTanggalAndLokasiStatus(tanggal, "DI_LUAR_KANTOR");
            totalBelumHadir = Math.max(0, totalUser - totalHadir);
        } else {
            // Summary keseluruhan
            totalHadir = attendanceRepository.count();
            totalDiKantor = attendanceRepository.countByLokasiStatus("DI_KANTOR");
            totalDiLuarKantor = attendanceRepository.countByLokasiStatus("DI_LUAR_KANTOR");
            totalBelumHadir = Math.max(0, totalUser - attendanceRepository.countByTanggal(LocalDate.now()));
        }

        return AttendanceSummaryDTO.builder()
                .tanggal(tanggal)
                .totalUser(totalUser)
                .totalHadir(totalHadir)
                .totalBelumHadir(totalBelumHadir)
                .totalDiKantor(totalDiKantor)
                .totalDiLuarKantor(totalDiLuarKantor)
                .build();
    }

    /**
     * Data lokasi absensi untuk peta Leaflet Admin
     */
    public List<AttendanceUserLocationDTO> getAdminLocations(LocalDate tanggal) {
        List<Attendance> attendances;
        if (tanggal != null) {
            attendances = attendanceRepository.findByTanggalOrderByCreatedAtDesc(tanggal);
        } else {
            attendances = attendanceRepository.findAllByOrderByTanggalDescCreatedAtDesc();
        }

        Map<String, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        return attendances.stream().map(a -> {
            User user = userMap.get(a.getUserId());
            return AttendanceUserLocationDTO.builder()
                    .id(a.getId())
                    .userId(a.getUserId())
                    .userName(user != null ? user.getNama() : "User " + a.getUserId())
                    .userRole(user != null ? user.getRole() : "-")
                    .tanggal(a.getTanggal())
                    .jamMasuk(a.getJamMasuk())
                    .jamPulang(a.getJamPulang())
                    .latitude(a.getLatitude())
                    .longitude(a.getLongitude())
                    .lokasiStatus(a.getLokasiStatus())
                    .jarakMeter(a.getJarakMeter())
                    .fotoMasuk(a.getFotoMasuk())
                    .pulangLatitude(a.getPulangLatitude())
                    .pulangLongitude(a.getPulangLongitude())
                    .pulangLokasiStatus(a.getPulangLokasiStatus())
                    .pulangJarakMeter(a.getPulangJarakMeter())
                    .fotoPulang(a.getFotoPulang())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Mengambil file foto selfie dari storage
     */
    public Resource getPhotoResource(String filename) {
        try {
            Path filePath = Paths.get(uploadAttendanceDir).resolve(filename).normalize();
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                return null;
            }
            return new FileSystemResource(file);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper untuk mapping entity ke DTO
     */
    private AttendanceResponseDTO mapToDTO(Attendance attendance, User user) {
        boolean completed = attendance.getJamMasuk() != null && attendance.getJamPulang() != null;
        return AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .userId(attendance.getUserId())
                .userName(user != null ? user.getNama() : null)
                .userRole(user != null ? user.getRole() : null)
                .tanggal(attendance.getTanggal())
                .jamMasuk(attendance.getJamMasuk())
                .jamPulang(attendance.getJamPulang())
                .latitude(attendance.getLatitude())
                .longitude(attendance.getLongitude())
                .lokasiStatus(attendance.getLokasiStatus())
                .jarakMeter(attendance.getJarakMeter())
                .gpsAccuracy(attendance.getGpsAccuracy())
                .fotoMasuk(attendance.getFotoMasuk())
                .pulangLatitude(attendance.getPulangLatitude())
                .pulangLongitude(attendance.getPulangLongitude())
                .pulangLokasiStatus(attendance.getPulangLokasiStatus())
                .pulangJarakMeter(attendance.getPulangJarakMeter())
                .pulangGpsAccuracy(attendance.getPulangGpsAccuracy())
                .fotoPulang(attendance.getFotoPulang())
                .deviceInfo(attendance.getDeviceInfo())
                .isCompleted(completed)
                .build();
    }

    /**
     * Menyimpan Base64 photo ke file sistem
     */
    private String savePhoto(String base64Data, String filePrefix) {
        try {
            // Bersihkan data URL header jika ada (misal: "data:image/jpeg;base64,...")
            String cleanBase64 = base64Data;
            if (base64Data.contains(",")) {
                cleanBase64 = base64Data.split(",")[1];
            }

            byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);

            File dir = new File(uploadAttendanceDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = filePrefix + ".jpg";
            File destinationFile = new File(dir, filename);

            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(decodedBytes);
            }

            return filename;
        } catch (Exception e) {
            System.err.println("Gagal menyimpan foto selfie absensi: " + e.getMessage());
            return null;
        }
    }
}
