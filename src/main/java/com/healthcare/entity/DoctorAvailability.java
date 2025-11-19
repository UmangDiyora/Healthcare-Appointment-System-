package com.healthcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Doctor Availability entity
 * Defines doctor's weekly availability schedule
 */
@Entity
@Table(name = "doctor_availability",
       indexes = {
           @Index(name = "idx_availability_doctor_day", columnList = "doctor_id,day_of_week")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_doctor_day_time",
                           columnNames = {"doctor_id", "day_of_week", "start_time"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration", nullable = false)
    @Builder.Default
    private Integer slotDuration = 30; // in minutes

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}
