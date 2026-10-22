package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserIdAndTanggal(String userId, LocalDate tanggal);

    List<Attendance> findByUserIdOrderByTanggalDesc(String userId);

    List<Attendance> findByUserIdAndTanggalBetweenOrderByTanggalDesc(String userId, LocalDate start, LocalDate end);

    List<Attendance> findByTanggalBetweenOrderByTanggalDesc(LocalDate start, LocalDate end);

    long countByTanggal(LocalDate tanggal);

    long countByTanggalAndLokasiStatus(LocalDate tanggal, String lokasiStatus);

    long countByLokasiStatus(String lokasiStatus);

    @Query("SELECT a FROM Attendance a ORDER BY a.tanggal DESC, a.createdAt DESC")
    List<Attendance> findAllByOrderByTanggalDescCreatedAtDesc();

    @Query("SELECT a FROM Attendance a WHERE a.userId IN (SELECT u.id FROM User u WHERE u.role = :role) ORDER BY a.tanggal DESC, a.createdAt DESC")
    List<Attendance> findByRoleOrderByTanggalDescCreatedAtDesc(@Param("role") String role);

    @Query("SELECT a FROM Attendance a WHERE a.tanggal = :tanggal ORDER BY a.createdAt DESC")
    List<Attendance> findByTanggalOrderByCreatedAtDesc(LocalDate tanggal);

    @Query("SELECT a FROM Attendance a WHERE a.tanggal = :tanggal AND a.userId IN (SELECT u.id FROM User u WHERE u.role = :role) ORDER BY a.createdAt DESC")
    List<Attendance> findByTanggalAndRole(@Param("tanggal") LocalDate tanggal, @Param("role") String role);

    @Query("SELECT a FROM Attendance a WHERE a.tanggal = :tanggal AND (:role IS NULL OR a.userId IN (SELECT u.id FROM User u WHERE u.role = :role)) ORDER BY a.createdAt DESC")
    List<Attendance> findByTanggalAndOptionalRole(@Param("tanggal") LocalDate tanggal, @Param("role") String role);
}
