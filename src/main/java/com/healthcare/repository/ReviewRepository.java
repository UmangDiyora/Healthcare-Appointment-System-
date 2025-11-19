package com.healthcare.repository;

import com.healthcare.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Review entity
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDoctorId(Long doctorId);

    List<Review> findByDoctorIdAndIsApprovedTrue(Long doctorId);

    List<Review> findByPatientId(Long patientId);

    Optional<Review> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT r FROM Review r WHERE r.doctor.id = :doctorId " +
           "AND r.isApproved = true " +
           "ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT r FROM Review r WHERE r.doctor.id = :doctorId " +
           "AND r.rating >= :minRating " +
           "AND r.isApproved = true")
    List<Review> findHighRatedReviews(@Param("doctorId") Long doctorId,
                                     @Param("minRating") Integer minRating);

    List<Review> findByIsApprovedFalse();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId AND r.isApproved = true")
    Double calculateAverageRatingForDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId AND r.isApproved = true")
    long countApprovedReviewsByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT r FROM Review r WHERE r.patient.id = :patientId " +
           "AND r.doctor.id = :doctorId")
    List<Review> findByPatientAndDoctor(@Param("patientId") Long patientId,
                                       @Param("doctorId") Long doctorId);
}
