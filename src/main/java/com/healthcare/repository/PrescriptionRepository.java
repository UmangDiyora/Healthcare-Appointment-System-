package com.healthcare.repository;

import com.healthcare.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Prescription entity
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByPatientIdAndIsActiveTrue(Long patientId);

    List<Prescription> findByDoctorId(Long doctorId);

    List<Prescription> findByAppointmentId(Long appointmentId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
           "AND p.isActive = true " +
           "ORDER BY p.prescribedDate DESC")
    List<Prescription> findActivePrescriptionsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
           "AND p.prescribedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.prescribedDate DESC")
    List<Prescription> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Prescription p WHERE p.doctor.id = :doctorId " +
           "AND p.patient.id = :patientId " +
           "ORDER BY p.prescribedDate DESC")
    List<Prescription> findByDoctorIdAndPatientId(@Param("doctorId") Long doctorId,
                                                  @Param("patientId") Long patientId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patient.id = :patientId AND p.isActive = true")
    long countActivePrescriptionsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.doctor.id = :doctorId")
    long countPrescriptionsByDoctor(@Param("doctorId") Long doctorId);
}
