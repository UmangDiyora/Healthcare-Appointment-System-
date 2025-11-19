package com.healthcare.service;

import com.healthcare.dto.request.UpdatePatientProfileRequest;
import com.healthcare.dto.response.PatientProfileResponse;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.entity.UserType;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedAccessException;
import com.healthcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PatientService patientService;

    private User user;
    private Patient patient;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("patient@test.com")
                .userType(UserType.PATIENT)
                .phoneNumber("+1234567890")
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .bloodGroup("O+")
                .build();
    }

    @Test
    void getPatientProfile_Success() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        // Act
        PatientProfileResponse response = patientService.getPatientProfile(user);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("patient@test.com", response.getEmail());
        verify(auditService).log(any(), eq("VIEW"), eq("PATIENT"), eq(1L));
    }

    @Test
    void getPatientProfile_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientProfile(user));
    }

    @Test
    void updatePatientProfile_Success() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        UpdatePatientProfileRequest request = UpdatePatientProfileRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .bloodGroup("A+")
                .build();

        // Act
        PatientProfileResponse response = patientService.updatePatientProfile(request, user);

        // Assert
        assertNotNull(response);
        verify(patientRepository).save(any(Patient.class));
        verify(auditService).log(any(), eq("UPDATE"), eq("PATIENT"), eq(1L));
    }

    @Test
    void getPatientById_UnauthorizedUser_ThrowsException() {
        // Arrange
        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .userType(UserType.PATIENT)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> patientService.getPatientById(1L, otherUser));
    }

    @Test
    void getPatientById_DoctorCanView_Success() {
        // Arrange
        User doctorUser = User.builder()
                .id(2L)
                .email("doctor@test.com")
                .userType(UserType.DOCTOR)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // Act
        PatientProfileResponse response = patientService.getPatientById(1L, doctorUser);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }
}
