package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.dto.request.SetAvailabilityRequest;
import com.healthcare.dto.request.UpdateDoctorProfileRequest;
import com.healthcare.dto.response.AvailabilityResponse;
import com.healthcare.dto.response.DoctorProfileResponse;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.DoctorAvailability;
import com.healthcare.entity.User;
import com.healthcare.exception.InvalidRequestException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AuditService auditService;

    @Auditable(action = "VIEW", entityType = "DOCTOR")
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(User currentUser) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        auditService.log(currentUser, "VIEW", "DOCTOR", doctor.getId());

        return mapToProfileResponse(doctor);
    }

    @Auditable(action = "VIEW", entityType = "DOCTOR")
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return mapToProfileResponse(doctor);
    }

    @Auditable(action = "UPDATE", entityType = "DOCTOR")
    @Transactional
    public DoctorProfileResponse updateDoctorProfile(UpdateDoctorProfileRequest request, User currentUser) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        // Update doctor fields
        if (request.getFirstName() != null) {
            doctor.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            doctor.setLastName(request.getLastName());
        }
        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getYearsOfExperience() != null) {
            doctor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getQualification() != null) {
            doctor.setQualification(request.getQualification());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }

        doctor = doctorRepository.save(doctor);

        auditService.log(currentUser, "UPDATE", "DOCTOR", doctor.getId());
        log.info("Doctor profile updated for user: {}", currentUser.getId());

        return mapToProfileResponse(doctor);
    }

    @Auditable(action = "CREATE", entityType = "DOCTOR_AVAILABILITY")
    @Transactional
    public AvailabilityResponse setAvailability(SetAvailabilityRequest request, User currentUser) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        // Validate times
        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new InvalidRequestException("Start time must be before end time");
        }

        // Parse day of week
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid day of week: " + request.getDayOfWeek());
        }

        // Check if availability already exists for this doctor and day
        List<DoctorAvailability> existingAvailability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek.name());

        // Check for overlapping time slots
        boolean hasOverlap = existingAvailability.stream()
                .anyMatch(avail ->
                        (request.getStartTime().isBefore(avail.getEndTime()) &&
                                request.getEndTime().isAfter(avail.getStartTime()))
                );

        if (hasOverlap) {
            throw new InvalidRequestException("Time slot overlaps with existing availability");
        }

        // Create new availability
        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(dayOfWeek.name())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDuration(request.getSlotDuration())
                .isAvailable(true)
                .build();

        availability = availabilityRepository.save(availability);

        auditService.log(currentUser, "CREATE", "DOCTOR_AVAILABILITY", availability.getId());
        log.info("Availability set for doctor: {} on {}", doctor.getId(), dayOfWeek);

        return mapToAvailabilityResponse(availability);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getDoctorAvailability(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(doctorId);

        return availabilities.stream()
                .map(this::mapToAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Auditable(action = "DELETE", entityType = "DOCTOR_AVAILABILITY")
    @Transactional
    public void deleteAvailability(Long availabilityId, User currentUser) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        // Verify the availability belongs to the current doctor
        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new InvalidRequestException("Cannot delete another doctor's availability");
        }

        availabilityRepository.delete(availability);

        auditService.log(currentUser, "DELETE", "DOCTOR_AVAILABILITY", availabilityId);
        log.info("Availability deleted: {}", availabilityId);
    }

    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> searchDoctors(String specialization) {
        List<Doctor> doctors;

        if (specialization != null && !specialization.isEmpty()) {
            doctors = doctorRepository.findBySpecializationContainingIgnoreCase(specialization);
        } else {
            doctors = doctorRepository.findAll();
        }

        // Filter only active doctors
        return doctors.stream()
                .filter(doctor -> doctor.getUser().getIsActive())
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    private DoctorProfileResponse mapToProfileResponse(Doctor doctor) {
        return DoctorProfileResponse.builder()
                .id(doctor.getId())
                .email(doctor.getUser().getEmail())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .qualification(doctor.getQualification())
                .bio(doctor.getBio())
                .consultationFee(doctor.getConsultationFee())
                .averageRating(doctor.getAverageRating())
                .totalReviews(doctor.getTotalReviews())
                .isActive(doctor.getUser().getIsActive())
                .build();
    }

    private AvailabilityResponse mapToAvailabilityResponse(DoctorAvailability availability) {
        return AvailabilityResponse.builder()
                .id(availability.getId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDuration(availability.getSlotDuration())
                .isAvailable(availability.getIsAvailable())
                .build();
    }
}
