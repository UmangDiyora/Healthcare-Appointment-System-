package com.healthcare.repository;

import com.healthcare.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Test
    void testUserRepository_SaveAndFind() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .password("hashedPassword")
                .userType(UserType.PATIENT)
                .isActive(true)
                .build();

        // When
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertNotNull(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testPatientRepository_FindByUserId() {
        // Given
        User user = createUser("patient@test.com", UserType.PATIENT);
        Patient patient = Patient.builder()
                .user(user)
                .firstName("John")
                .lastName("Doe")
                .build();
        entityManager.persist(patient);
        entityManager.flush();

        // When
        Optional<Patient> found = patientRepository.findByUserId(user.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testDoctorRepository_FindBySpecialization() {
        // Given
        User doctorUser = createUser("doctor@test.com", UserType.DOCTOR);
        Doctor doctor = Doctor.builder()
                .user(doctorUser)
                .firstName("Jane")
                .lastName("Smith")
                .specialization("Cardiology")
                .licenseNumber("MD123")
                .isApproved(true)
                .build();
        entityManager.persist(doctor);
        entityManager.flush();

        // When
        List<Doctor> doctors = doctorRepository.findBySpecializationContainingIgnoreCase("cardio");

        // Then
        assertFalse(doctors.isEmpty());
        assertEquals("Cardiology", doctors.get(0).getSpecialization());
    }

    @Test
    void testAppointmentRepository_CheckAvailabilityWithLock() {
        // Given
        User patientUser = createUser("patient@test.com", UserType.PATIENT);
        User doctorUser = createUser("doctor@test.com", UserType.DOCTOR);

        Patient patient = Patient.builder().user(patientUser).firstName("John").lastName("Doe").build();
        entityManager.persist(patient);

        Doctor doctor = Doctor.builder()
                .user(doctorUser)
                .firstName("Jane")
                .lastName("Smith")
                .licenseNumber("MD123")
                .isApproved(true)
                .build();
        entityManager.persist(doctor);

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        // When - Check availability (should be true - no appointment)
        boolean available = appointmentRepository.checkAvailabilityWithLock(
                doctor.getId(), date, time);

        // Then
        assertTrue(available);

        // Create appointment
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .appointmentTime(time)
                .status(AppointmentStatus.SCHEDULED)
                .appointmentType(AppointmentType.IN_PERSON)
                .duration(30)
                .build();
        entityManager.persist(appointment);
        entityManager.flush();

        // When - Check again (should be false - slot taken)
        boolean stillAvailable = appointmentRepository.checkAvailabilityWithLock(
                doctor.getId(), date, time);

        // Then
        assertFalse(stillAvailable);
    }

    @Test
    void testDoctorAvailabilityRepository_FindByDoctorAndDay() {
        // Given
        User doctorUser = createUser("doctor@test.com", UserType.DOCTOR);
        Doctor doctor = Doctor.builder()
                .user(doctorUser)
                .firstName("Jane")
                .lastName("Smith")
                .licenseNumber("MD123")
                .isApproved(true)
                .build();
        entityManager.persist(doctor);

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDuration(30)
                .isAvailable(true)
                .build();
        entityManager.persist(availability);
        entityManager.flush();

        // When
        List<DoctorAvailability> found = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctor.getId(), "MONDAY");

        // Then
        assertFalse(found.isEmpty());
        assertEquals(LocalTime.of(9, 0), found.get(0).getStartTime());
        assertEquals(30, found.get(0).getSlotDuration());
    }

    private User createUser(String email, UserType userType) {
        User user = User.builder()
                .email(email)
                .password("password")
                .userType(userType)
                .isActive(true)
                .build();
        entityManager.persist(user);
        return user;
    }
}
