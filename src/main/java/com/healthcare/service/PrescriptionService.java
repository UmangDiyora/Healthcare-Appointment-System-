package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.dto.request.CreatePrescriptionRequest;
import com.healthcare.dto.response.PrescriptionResponse;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.AppointmentStatus;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Prescription;
import com.healthcare.entity.User;
import com.healthcare.exception.InvalidRequestException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedAccessException;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Auditable(action = "CREATE", entityType = "PRESCRIPTION")
    @Transactional
    public PrescriptionResponse createPrescription(
            CreatePrescriptionRequest request,
            User currentUser) {

        // 1. Validate doctor
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can create prescriptions"));

        // 2. Validate appointment
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 3. Validate doctor owns the appointment
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedAccessException("Cannot prescribe for another doctor's appointment");
        }

        // 4. Validate appointment is completed
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidRequestException("Can only create prescriptions for completed appointments");
        }

        // 5. Create prescription
        Prescription prescription = Prescription.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(doctor)
                .medicationName(request.getMedicationName())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .duration(request.getDuration())
                .instructions(request.getInstructions())
                .refills(request.getRefills())
                .isActive(true)
                .prescribedDate(LocalDate.now())
                .build();

        prescription = prescriptionRepository.save(prescription);

        // 6. Send notification to patient
        notificationService.notifyNewPrescription(prescription);

        // 7. Audit log
        auditService.log(currentUser, "CREATE", "PRESCRIPTION", prescription.getId());

        log.info("Prescription created: {} for patient: {} by doctor: {}",
                prescription.getId(), appointment.getPatient().getId(), doctor.getId());

        return mapToResponse(prescription);
    }

    @Auditable(action = "VIEW", entityType = "PRESCRIPTION")
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(Long prescriptionId, User currentUser) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        // Validate access
        validateAccessToPrescription(prescription, currentUser);

        auditService.log(currentUser, "VIEW", "PRESCRIPTION", prescriptionId);

        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId, User currentUser, Boolean activeOnly) {
        // Validate access - patient can view own, doctors can view all
        validatePatientAccess(patientId, currentUser);

        List<Prescription> prescriptions;
        if (activeOnly != null && activeOnly) {
            prescriptions = prescriptionRepository.findByPatientIdAndIsActiveTrue(patientId);
        } else {
            prescriptions = prescriptionRepository.findByPatientIdOrderByPrescribedDateDesc(patientId);
        }

        auditService.log(currentUser, "VIEW", "PRESCRIPTION_LIST", patientId);

        return prescriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyPrescriptions(User currentUser, Boolean activeOnly) {
        // For patients viewing their own prescriptions
        Long patientId;

        if (currentUser.getUserType().toString().equals("PATIENT")) {
            patientId = currentUser.getId(); // This should be patient entity ID
            return getPatientPrescriptions(patientId, currentUser, activeOnly);
        } else {
            throw new UnauthorizedAccessException("Only patients can view their own prescriptions");
        }
    }

    @Auditable(action = "UPDATE", entityType = "PRESCRIPTION")
    @Transactional
    public PrescriptionResponse deactivatePrescription(Long prescriptionId, User currentUser) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        // Only the prescribing doctor can deactivate
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can deactivate prescriptions"));

        if (!prescription.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedAccessException("Cannot deactivate another doctor's prescription");
        }

        prescription.setIsActive(false);
        prescription = prescriptionRepository.save(prescription);

        auditService.log(currentUser, "UPDATE", "PRESCRIPTION", prescriptionId);

        log.info("Prescription deactivated: {} by doctor: {}", prescriptionId, doctor.getId());

        return mapToResponse(prescription);
    }

    // Helper methods

    private void validateAccessToPrescription(Prescription prescription, User currentUser) {
        boolean isPatient = prescription.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isDoctor = prescription.getDoctor().getUser().getId().equals(currentUser.getId());

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to this prescription");
        }
    }

    private void validatePatientAccess(Long patientId, User currentUser) {
        // Patient can view own prescriptions, doctors can view any patient's prescriptions
        boolean isOwnRecord = currentUser.getId().equals(patientId);
        boolean isDoctor = currentUser.getUserType().toString().equals("DOCTOR");

        if (!isOwnRecord && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to patient prescriptions");
        }
    }

    private PrescriptionResponse mapToResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .appointmentId(prescription.getAppointment().getId())
                .patientId(prescription.getPatient().getId())
                .patientName(prescription.getPatient().getFirstName() + " " +
                        prescription.getPatient().getLastName())
                .doctorId(prescription.getDoctor().getId())
                .doctorName("Dr. " + prescription.getDoctor().getFirstName() + " " +
                        prescription.getDoctor().getLastName())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .duration(prescription.getDuration())
                .instructions(prescription.getInstructions())
                .refills(prescription.getRefills())
                .isActive(prescription.getIsActive())
                .prescribedDate(prescription.getPrescribedDate())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
