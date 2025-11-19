package com.healthcare.repository;

import com.healthcare.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for DoctorAvailability entity
 */
@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true")
    List<DoctorAvailability> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.isAvailable = true")
    List<DoctorAvailability> findAllAvailableSlotsByDoctor(@Param("doctorId") Long doctorId);

    void deleteByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek);
}
