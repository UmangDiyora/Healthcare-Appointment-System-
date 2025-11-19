package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.dto.TimeSlot;
import com.healthcare.dto.request.CancelAppointmentRequest;
import com.healthcare.dto.request.CreateAppointmentRequest;
import com.healthcare.dto.request.RescheduleAppointmentRequest;
import com.healthcare.dto.response.AppointmentResponse;
import com.healthcare.dto.response.DoctorAvailabilitySlotResponse;
import com.healthcare.entity.*;
import com.healthcare.exception.AppointmentConflictException;
import com.healthcare.exception.InvalidRequestException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedAccessException;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Auditable(action = "CREATE", entityType = "APPOINTMENT")
    @Transactional
    public AppointmentResponse bookAppointment(CreateAppointmentRequest request, User currentUser) {
        // 1. Validate patient access
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only patients can book appointments"));

        // 2. Validate doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.getUser().getIsActive()) {
            throw new InvalidRequestException("Doctor is not available");
        }

        // 3. Validate date is not in the past
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Cannot book appointment in the past");
        }

        // 4. Check if slot is in doctor's availability schedule
        if (!isSlotInDoctorSchedule(request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime())) {
            throw new InvalidRequestException("Doctor is not available at this time");
        }

        // 5. Check for conflicts (pessimistic locking to prevent race conditions)
        boolean isAvailable = appointmentRepository.checkAvailabilityWithLock(
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getAppointmentTime()
        );

        if (!isAvailable) {
            throw new AppointmentConflictException("This time slot is no longer available");
        }

        // 6. Get slot duration from doctor's availability
        Integer duration = getSlotDuration(request.getDoctorId(), request.getAppointmentDate());

        // 7. Create appointment
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .duration(duration)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(request.getAppointmentType())
                .reason(request.getReason())
                .symptoms(request.getSymptoms())
                .build();

        appointment = appointmentRepository.save(appointment);

        // 8. Send notifications
        notificationService.sendAppointmentConfirmation(appointment);
        notificationService.scheduleReminder(appointment, 24); // 24 hours before

        // 9. Audit log
        auditService.log(currentUser, "CREATE", "APPOINTMENT", appointment.getId());

        log.info("Appointment booked: {} for patient: {} with doctor: {}",
                appointment.getId(), patient.getId(), doctor.getId());

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilitySlotResponse getAvailableSlots(Long doctorId, LocalDate date) {
        // 1. Get doctor
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // 2. Get doctor's availability for the day
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek.name());

        if (availabilities.isEmpty()) {
            return DoctorAvailabilitySlotResponse.builder()
                    .doctorId(doctorId)
                    .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                    .specialization(doctor.getSpecialization())
                    .consultationFee(doctor.getConsultationFee())
                    .averageRating(doctor.getAverageRating())
                    .date(date)
                    .availableSlots(Collections.emptyList())
                    .build();
        }

        // 3. Generate all possible slots
        List<TimeSlot> allSlots = new ArrayList<>();
        for (DoctorAvailability avail : availabilities) {
            if (avail.getIsAvailable()) {
                allSlots.addAll(generateSlots(
                        avail.getStartTime(),
                        avail.getEndTime(),
                        avail.getSlotDuration()
                ));
            }
        }

        // 4. Get booked appointments for the day
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNot(
                        doctorId,
                        date,
                        AppointmentStatus.CANCELLED
                );

        // 5. Filter out booked slots
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        // 6. Filter out past slots if date is today
        LocalTime now = LocalTime.now();
        List<TimeSlot> availableSlots = allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot.getStartTime()))
                .filter(slot -> {
                    if (date.equals(LocalDate.now())) {
                        return slot.getStartTime().isAfter(now.plusMinutes(30)); // 30 min buffer
                    }
                    return true;
                })
                .map(slot -> {
                    slot.setIsAvailable(true);
                    return slot;
                })
                .collect(Collectors.toList());

        return DoctorAvailabilitySlotResponse.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                .specialization(doctor.getSpecialization())
                .consultationFee(doctor.getConsultationFee())
                .averageRating(doctor.getAverageRating())
                .date(date)
                .availableSlots(availableSlots)
                .build();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Validate access
        validateUserAccess(appointment, currentUser);

        auditService.log(currentUser, "VIEW", "APPOINTMENT", appointmentId);

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(User currentUser) {
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patient.getId());

        auditService.log(currentUser, "VIEW", "APPOINTMENT_LIST", patient.getId());

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(User currentUser, LocalDate date) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<Appointment> appointments;
        if (date != null) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), date);
        } else {
            appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateDesc(doctor.getId());
        }

        auditService.log(currentUser, "VIEW", "APPOINTMENT_LIST", doctor.getId());

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Auditable(action = "UPDATE", entityType = "APPOINTMENT")
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, CancelAppointmentRequest request, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Validate access - only patient or doctor can cancel
        validateUserAccess(appointment, currentUser);

        // Cannot cancel completed or already cancelled appointments
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidRequestException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot cancel completed appointment");
        }

        // Update status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(request.getCancellationReason());
        appointment = appointmentRepository.save(appointment);

        // Send cancellation notification
        notificationService.sendAppointmentCancellation(appointment, request.getCancellationReason());

        auditService.log(currentUser, "UPDATE", "APPOINTMENT", appointmentId);
        log.info("Appointment cancelled: {} by user: {}", appointmentId, currentUser.getId());

        return mapToResponse(appointment);
    }

    @Auditable(action = "UPDATE", entityType = "APPOINTMENT")
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, RescheduleAppointmentRequest request, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Only patients can reschedule
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only patients can reschedule"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new UnauthorizedAccessException("Cannot reschedule another patient's appointment");
        }

        // Cannot reschedule cancelled or completed appointments
        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot reschedule " +
                    appointment.getStatus().name().toLowerCase() + " appointment");
        }

        // Validate new date is not in the past
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Cannot reschedule to a past date");
        }

        // Check if new slot is available
        boolean isAvailable = appointmentRepository.checkAvailabilityWithLock(
                appointment.getDoctor().getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime()
        );

        if (!isAvailable) {
            throw new AppointmentConflictException("The requested time slot is not available");
        }

        // Check if slot is in doctor's schedule
        if (!isSlotInDoctorSchedule(appointment.getDoctor().getId(),
                request.getAppointmentDate(), request.getAppointmentTime())) {
            throw new InvalidRequestException("Doctor is not available at this time");
        }

        // Update appointment
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment = appointmentRepository.save(appointment);

        auditService.log(currentUser, "UPDATE", "APPOINTMENT", appointmentId);
        log.info("Appointment rescheduled: {} to {} at {}",
                appointmentId, request.getAppointmentDate(), request.getAppointmentTime());

        return mapToResponse(appointment);
    }

    @Auditable(action = "UPDATE", entityType = "APPOINTMENT")
    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Only doctor can complete appointment
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can complete appointments"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedAccessException("Cannot complete another doctor's appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidRequestException("Appointment is already completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);

        auditService.log(currentUser, "UPDATE", "APPOINTMENT", appointmentId);
        log.info("Appointment completed: {}", appointmentId);

        return mapToResponse(appointment);
    }

    // Helper methods

    private boolean isSlotInDoctorSchedule(Long doctorId, LocalDate date, LocalTime time) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek.name());

        return availabilities.stream()
                .filter(DoctorAvailability::getIsAvailable)
                .anyMatch(avail ->
                        !time.isBefore(avail.getStartTime()) &&
                                time.plusMinutes(avail.getSlotDuration()).isBefore(avail.getEndTime()) ||
                                time.plusMinutes(avail.getSlotDuration()).equals(avail.getEndTime())
                );
    }

    private Integer getSlotDuration(Long doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek.name());

        return availabilities.stream()
                .filter(DoctorAvailability::getIsAvailable)
                .findFirst()
                .map(DoctorAvailability::getSlotDuration)
                .orElse(30); // Default 30 minutes
    }

    private List<TimeSlot> generateSlots(LocalTime start, LocalTime end, int duration) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = start;

        while (current.plusMinutes(duration).isBefore(end) ||
                current.plusMinutes(duration).equals(end)) {
            slots.add(TimeSlot.builder()
                    .startTime(current)
                    .endTime(current.plusMinutes(duration))
                    .isAvailable(true)
                    .build());
            current = current.plusMinutes(duration);
        }

        return slots;
    }

    private void validateUserAccess(Appointment appointment, User currentUser) {
        boolean isPatient = appointment.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(currentUser.getId());

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to this appointment");
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName("Dr. " + appointment.getDoctor().getFirstName() + " " +
                        appointment.getDoctor().getLastName())
                .specialization(appointment.getDoctor().getSpecialization())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .duration(appointment.getDuration())
                .status(appointment.getStatus())
                .appointmentType(appointment.getAppointmentType())
                .reason(appointment.getReason())
                .symptoms(appointment.getSymptoms())
                .diagnosis(appointment.getDiagnosis())
                .prescription(appointment.getPrescription())
                .notes(appointment.getNotes())
                .twilioRoomSid(appointment.getTwilioRoomSid())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
