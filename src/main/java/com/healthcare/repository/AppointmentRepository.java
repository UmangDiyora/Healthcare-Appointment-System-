package com.healthcare.repository;

import com.healthcare.entity.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Appointment entity
 * Includes pessimistic locking for conflict prevention
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Patient appointments
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :fromDate ORDER BY a.appointmentDate, a.appointmentTime")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId, @Param("fromDate") LocalDate fromDate);

    // Doctor appointments
    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :fromDate ORDER BY a.appointmentDate, a.appointmentTime")
    List<Appointment> findUpcomingAppointmentsByDoctor(@Param("doctorId") Long doctorId, @Param("fromDate") LocalDate fromDate);

    // Conflict detection with pessimistic locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT CASE WHEN COUNT(a) = 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND a.appointmentTime = :time " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    boolean checkAvailabilityWithLock(@Param("doctorId") Long doctorId,
                                     @Param("date") LocalDate date,
                                     @Param("time") LocalTime time);

    // Find appointments excluding a specific status
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(
        Long doctorId,
        LocalDate appointmentDate,
        Appointment.AppointmentStatus status
    );

    // Check if slot is taken (without lock)
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
        Long doctorId,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        List<Appointment.AppointmentStatus> statuses
    );

    // Find by confirmation number
    Optional<Appointment> findByConfirmationNumber(String confirmationNumber);

    // Appointments needing reminders
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.appointmentDate >= CURRENT_DATE")
    List<Appointment> findAppointmentsForDate(@Param("date") LocalDate date);

    // Statistics queries
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findAppointmentsBetweenDates(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // For video consultations
    Optional<Appointment> findByTwilioRoomSid(String roomSid);

    List<Appointment> findByAppointmentTypeAndStatus(Appointment.AppointmentType type, Appointment.AppointmentStatus status);
}
