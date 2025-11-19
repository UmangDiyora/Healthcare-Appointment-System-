package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Doctor entity
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    @Query("SELECT d FROM Doctor d WHERE d.user.email = :email")
    Optional<Doctor> findByUserEmail(@Param("email") String email);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);

    Page<Doctor> findBySpecialization(String specialization, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.user.isActive = true AND d.isApproved = true")
    List<Doctor> findAllActiveAndApprovedDoctors();

    @Query("SELECT d FROM Doctor d WHERE d.user.isActive = true AND d.isApproved = true AND d.specialization = :specialization")
    List<Doctor> findActiveApprovedDoctorsBySpecialization(@Param("specialization") String specialization);

    List<Doctor> findByIsApprovedFalse();

    @Query("SELECT d FROM Doctor d WHERE d.averageRating >= :minRating ORDER BY d.averageRating DESC")
    List<Doctor> findTopRatedDoctors(@Param("minRating") Double minRating);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.user.isActive = true AND d.isApproved = true ORDER BY d.specialization")
    List<String> findAllActiveSpecializations();

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.user.isActive = true AND d.isApproved = true")
    long countActiveApprovedDoctors();
}
