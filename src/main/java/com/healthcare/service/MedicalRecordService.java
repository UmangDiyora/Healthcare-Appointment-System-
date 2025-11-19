package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.dto.request.CreateMedicalRecordRequest;
import com.healthcare.dto.response.MedicalRecordResponse;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.MedicalRecord;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.exception.*;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.MedicalRecordRepository;
import com.healthcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final S3Service s3Service;
    private final AuditService auditService;
    private final NotificationService notificationService;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/dicom"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Auditable(action = "CREATE", entityType = "MEDICAL_RECORD")
    @Transactional
    public MedicalRecordResponse uploadMedicalRecord(
            MultipartFile file,
            CreateMedicalRecordRequest request,
            User currentUser) {

        // 1. Validate doctor access
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can upload medical records"));

        // 2. Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // 3. Validate file
        validateFile(file);

        // 4. Prepare S3 metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("patient-id", request.getPatientId().toString());
        metadata.put("doctor-id", doctor.getId().toString());
        metadata.put("record-type", request.getRecordType().toString());

        // 5. Upload to S3
        String fileKey = s3Service.uploadFile(
                file,
                "medical-records/" + request.getPatientId(),
                metadata
        );

        // 6. Create database record
        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentId(request.getAppointmentId())
                .recordType(request.getRecordType())
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(fileKey)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .recordDate(request.getRecordDate())
                .build();

        record = medicalRecordRepository.save(record);

        // 7. Send notification to patient
        notificationService.notifyNewMedicalRecord(record);

        // 8. Audit log
        auditService.log(currentUser, "CREATE", "MEDICAL_RECORD", record.getId());

        log.info("Medical record uploaded: {} for patient: {} by doctor: {}",
                record.getId(), patient.getId(), doctor.getId());

        return mapToResponse(record);
    }

    @Auditable(action = "VIEW", entityType = "MEDICAL_RECORD")
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecord(Long recordId, User currentUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        // Validate access
        validateAccessToRecord(record, currentUser);

        auditService.log(currentUser, "VIEW", "MEDICAL_RECORD", recordId);

        return mapToResponse(record);
    }

    @Auditable(action = "VIEW", entityType = "MEDICAL_RECORD")
    @Transactional(readOnly = true)
    public byte[] downloadMedicalRecord(Long recordId, User currentUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        // Validate access
        validateAccessToRecord(record, currentUser);

        // Download from S3
        byte[] fileData = s3Service.downloadFile(record.getFileUrl());

        auditService.log(currentUser, "DOWNLOAD", "MEDICAL_RECORD", recordId);

        log.info("Medical record downloaded: {} by user: {}", recordId, currentUser.getId());

        return fileData;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getPatientMedicalRecords(Long patientId, User currentUser) {
        // Validate access
        validatePatientAccess(patientId, currentUser);

        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdOrderByRecordDateDesc(patientId);

        auditService.log(currentUser, "VIEW", "MEDICAL_RECORD_LIST", patientId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Auditable(action = "DELETE", entityType = "MEDICAL_RECORD")
    @Transactional
    public void deleteMedicalRecord(Long recordId, User currentUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        // Only doctor who created it can delete
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Only doctors can delete medical records"));

        if (!record.getDoctor().getId().equals(doctor.getId())) {
            throw new UnauthorizedAccessException("Cannot delete another doctor's medical record");
        }

        // Delete file from S3
        s3Service.deleteFile(record.getFileUrl());

        // Delete database record
        medicalRecordRepository.delete(record);

        auditService.log(currentUser, "DELETE", "MEDICAL_RECORD", recordId);

        log.info("Medical record deleted: {} by doctor: {}", recordId, doctor.getId());
    }

    // Helper methods

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is required");
        }

        // Validate file type
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException("File type not allowed. Allowed types: PDF, JPEG, PNG, DICOM");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("File size exceeds maximum limit of 10MB");
        }
    }

    private void validateAccessToRecord(MedicalRecord record, User currentUser) {
        boolean isPatient = record.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isDoctor = record.getDoctor().getUser().getId().equals(currentUser.getId());

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to this medical record");
        }
    }

    private void validatePatientAccess(Long patientId, User currentUser) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        boolean isOwnRecord = patient.getUser().getId().equals(currentUser.getId());
        boolean isDoctor = currentUser.getUserType().toString().equals("DOCTOR");

        if (!isOwnRecord && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to patient records");
        }
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .patientName(record.getPatient().getFirstName() + " " + record.getPatient().getLastName())
                .doctorId(record.getDoctor().getId())
                .doctorName("Dr. " + record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName())
                .appointmentId(record.getAppointmentId())
                .recordType(record.getRecordType())
                .title(record.getTitle())
                .description(record.getDescription())
                .fileUrl(record.getFileUrl())
                .fileType(record.getFileType())
                .fileSize(record.getFileSize())
                .recordDate(record.getRecordDate())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
