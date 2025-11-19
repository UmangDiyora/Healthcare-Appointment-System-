package com.healthcare.repository;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    @Query("SELECT p FROM Patient p WHERE p.user.email = :email")
    Optional<Patient> findByUserEmail(@Param("email") String email);

    @Query("SELECT p FROM Patient p WHERE p.user.isActive = true")
    List<Patient> findAllActivePatients();

    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.user.isActive = true")
    long countActivePatients();
}
