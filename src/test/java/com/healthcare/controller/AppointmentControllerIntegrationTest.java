package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.request.CreateAppointmentRequest;
import com.healthcare.entity.*;
import com.healthcare.repository.*;
import com.healthcare.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patientToken;
    private String doctorToken;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        // Create patient user
        User patientUser = User.builder()
                .email("patient@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.PATIENT)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        patientUser = userRepository.save(patientUser);

        patient = Patient.builder()
                .user(patientUser)
                .firstName("John")
                .lastName("Doe")
                .build();
        patient = patientRepository.save(patient);

        patientToken = tokenProvider.generateTokenFromUserId(patientUser.getId());

        // Create doctor user
        User doctorUser = User.builder()
                .email("doctor@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.DOCTOR)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        doctorUser = userRepository.save(doctorUser);

        doctor = Doctor.builder()
                .user(doctorUser)
                .firstName("Jane")
                .lastName("Smith")
                .specialization("Cardiology")
                .licenseNumber("MD12345")
                .isApproved(true)
                .build();
        doctor = doctorRepository.save(doctor);

        doctorToken = tokenProvider.generateTokenFromUserId(doctorUser.getId());

        // Create doctor availability
        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(LocalDate.now().plusDays(1).getDayOfWeek().name())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDuration(30)
                .isAvailable(true)
                .build();
        availabilityRepository.save(availability);
    }

    @Test
    void bookAppointment_ValidRequest_ReturnsCreated() throws Exception {
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .doctorId(doctor.getId())
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .reason("Checkup")
                .build();

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.doctorId").value(doctor.getId()))
                .andExpect(jsonPath("$.patientId").value(patient.getId()));
    }

    @Test
    void bookAppointment_WithoutAuth_ReturnsUnauthorized() throws Exception {
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .doctorId(doctor.getId())
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .appointmentType(AppointmentType.IN_PERSON)
                .build();

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailableSlots_ValidRequest_ReturnsSlots() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/appointments/availability")
                        .param("doctorId", doctor.getId().toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctor.getId()))
                .andExpect(jsonPath("$.availableSlots").isArray());
    }

    @Test
    void getPatientAppointments_WithAuth_ReturnsAppointments() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/my-appointments")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorAppointments_WithDoctorAuth_ReturnsAppointments() throws Exception {
        mockMvc.perform(get("/api/appointments/doctor/my-appointments")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorAppointments_WithPatientAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/appointments/doctor/my-appointments")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
