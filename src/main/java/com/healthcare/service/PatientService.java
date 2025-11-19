package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.dto.request.UpdatePatientProfileRequest;
import com.healthcare.dto.response.PatientProfileResponse;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedAccessException;
import com.healthcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Auditable(action = "VIEW", entityType = "PATIENT")
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(User currentUser) {
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        auditService.log(currentUser, "VIEW", "PATIENT", patient.getId());

        return mapToProfileResponse(patient);
    }

    @Auditable(action = "VIEW", entityType = "PATIENT")
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientById(Long patientId, User currentUser) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Validate access - patient can view own profile, doctors can view assigned patients
        if (!patient.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getUserType().toString().equals("DOCTOR")) {
            throw new UnauthorizedAccessException("Access denied to patient profile");
        }

        auditService.log(currentUser, "VIEW", "PATIENT", patientId);

        return mapToProfileResponse(patient);
    }

    @Auditable(action = "UPDATE", entityType = "PATIENT")
    @Transactional
    public PatientProfileResponse updatePatientProfile(UpdatePatientProfileRequest request, User currentUser) {
        Patient patient = patientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        // Update patient fields
        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }
        if (request.getBloodGroup() != null) {
            patient.setBloodGroup(request.getBloodGroup());
        }
        if (request.getPhoneNumber() != null) {
            patient.getUser().setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getInsuranceProvider() != null) {
            patient.setInsuranceProvider(request.getInsuranceProvider());
        }
        if (request.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(request.getInsuranceNumber());
        }
        if (request.getMedicalHistorySummary() != null) {
            patient.setMedicalHistorySummary(request.getMedicalHistorySummary());
        }
        if (request.getAllergies() != null) {
            patient.setAllergies(request.getAllergies());
        }

        patient = patientRepository.save(patient);

        auditService.log(currentUser, "UPDATE", "PATIENT", patient.getId());
        log.info("Patient profile updated for user: {}", currentUser.getId());

        return mapToProfileResponse(patient);
    }

    private PatientProfileResponse mapToProfileResponse(Patient patient) {
        return PatientProfileResponse.builder()
                .id(patient.getId())
                .email(patient.getUser().getEmail())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .phoneNumber(patient.getUser().getPhoneNumber())
                .address(patient.getAddress())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .insuranceProvider(patient.getInsuranceProvider())
                .insuranceNumber(patient.getInsuranceNumber())
                .medicalHistorySummary(patient.getMedicalHistorySummary())
                .allergies(patient.getAllergies())
                .build();
    }
}
