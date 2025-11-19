package com.healthcare.repository;

import com.healthcare.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for MedicalRecord entity
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);

    List<MedicalRecord> findByDoctorId(Long doctorId);

    List<MedicalRecord> findByAppointmentId(Long appointmentId);

    List<MedicalRecord> findByPatientIdAndRecordType(Long patientId, MedicalRecord.RecordType recordType);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId " +
           "AND mr.recordDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctor.id = :doctorId " +
           "AND mr.patient.id = :patientId " +
           "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findByDoctorIdAndPatientId(@Param("doctorId") Long doctorId,
                                                   @Param("patientId") Long patientId);

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    List<MedicalRecord> findByRecordType(MedicalRecord.RecordType recordType);
}
