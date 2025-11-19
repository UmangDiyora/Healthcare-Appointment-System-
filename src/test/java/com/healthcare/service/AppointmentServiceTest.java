package com.healthcare.service;

import com.healthcare.dto.TimeSlot;
import com.healthcare.dto.request.CancelAppointmentRequest;
import com.healthcare.dto.request.CreateAppointmentRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patientUser;
    private Patient patient;
    private User doctorUser;
    private Doctor doctor;
    private CreateAppointmentRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup patient
        patientUser = User.builder()
                .id(1L)
                .email("patient@test.com")
                .userType(UserType.PATIENT)
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .firstName("John")
                .lastName("Doe")
                .build();

        // Setup doctor
        doctorUser = User.builder()
                .id(2L)
                .email("doctor@test.com")
                .userType(UserType.DOCTOR)
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .firstName("Jane")
                .lastName("Smith")
                .specialization("Cardiology")
                .build();

        // Setup request
        createRequest = CreateAppointmentRequest.builder()
                .doctorId(1L)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .reason("Checkup")
                .build();
    }

    @Test
    void bookAppointment_Success() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.checkAvailabilityWithLock(any(), any(), any())).thenReturn(true);
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Arrays.asList(createMockAvailability()));

        Appointment savedAppointment = createMockAppointment();
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        AppointmentResponse response = appointmentService.bookAppointment(createRequest, patientUser);

        // Assert
        assertNotNull(response);
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
        verify(notificationService).sendAppointmentConfirmation(any(Appointment.class));
        verify(notificationService).scheduleReminder(any(Appointment.class), eq(24));
        verify(auditService).log(any(), eq("CREATE"), eq("APPOINTMENT"), any());
    }

    @Test
    void bookAppointment_SlotNotAvailable_ThrowsConflictException() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.checkAvailabilityWithLock(any(), any(), any())).thenReturn(false);

        // Act & Assert
        assertThrows(AppointmentConflictException.class,
                () -> appointmentService.bookAppointment(createRequest, patientUser));

        verify(appointmentRepository, never()).save(any());
        verify(notificationService, never()).sendAppointmentConfirmation(any());
    }

    @Test
    void bookAppointment_PatientNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> appointmentService.bookAppointment(createRequest, patientUser));
    }

    @Test
    void bookAppointment_DoctorNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(createRequest, patientUser));
    }

    @Test
    void getAvailableSlots_Success() {
        // Arrange
        LocalDate date = LocalDate.now().plusDays(1);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Arrays.asList(createMockAvailability()));
        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        DoctorAvailabilitySlotResponse response = appointmentService.getAvailableSlots(1L, date);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getDoctorId());
        assertFalse(response.getAvailableSlots().isEmpty());
    }

    @Test
    void cancelAppointment_Success() {
        // Arrange
        Appointment appointment = createMockAppointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest();
        cancelRequest.setCancellationReason("Personal reasons");

        // Act
        AppointmentResponse response = appointmentService.cancelAppointment(1L, cancelRequest, patientUser);

        // Assert
        assertNotNull(response);
        verify(notificationService).sendAppointmentCancellation(any(), any());
        verify(auditService).log(any(), eq("UPDATE"), eq("APPOINTMENT"), any());
    }

    @Test
    void cancelAppointment_AlreadyCancelled_ThrowsException() {
        // Arrange
        Appointment appointment = createMockAppointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        CancelAppointmentRequest cancelRequest = new CancelAppointmentRequest();

        // Act & Assert
        assertThrows(InvalidRequestException.class,
                () -> appointmentService.cancelAppointment(1L, cancelRequest, patientUser));
    }

    // Helper methods
    private DoctorAvailability createMockAvailability() {
        return DoctorAvailability.builder()
                .id(1L)
                .doctor(doctor)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDuration(30)
                .isAvailable(true)
                .build();
    }

    private Appointment createMockAppointment() {
        return Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .duration(30)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(AppointmentType.IN_PERSON)
                .build();
    }
}
