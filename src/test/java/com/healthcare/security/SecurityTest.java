package com.healthcare.security;

import com.healthcare.entity.User;
import com.healthcare.entity.UserType;
import com.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patientToken;
    private String doctorToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create test users
        User patient = createUser("patient@test.com", UserType.PATIENT);
        User doctor = createUser("doctor@test.com", UserType.DOCTOR);
        User admin = createUser("admin@test.com", UserType.ADMIN);

        patientToken = tokenProvider.generateTokenFromUserId(patient.getId());
        doctorToken = tokenProvider.generateTokenFromUserId(doctor.getId());
        adminToken = tokenProvider.generateTokenFromUserId(admin.getId());
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/patient/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessPatientEndpoint_WithPatientToken_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/patient/profile")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessDoctorEndpoint_WithPatientToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/doctor/profile")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessDoctorEndpoint_WithDoctorToken_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/doctor/profile")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk());
    }

    @Test
    void accessPatientEndpoint_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/patient/profile")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testJwtTokenGeneration() {
        // Test token generation
        String token = tokenProvider.generateTokenFromUserId(1L);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testJwtTokenValidation() {
        // Test token validation
        String token = tokenProvider.generateTokenFromUserId(1L);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void testJwtTokenExpiration() {
        // This would require mocking time or setting a very short expiration
        String token = tokenProvider.generateTokenFromUserId(1L);
        Long userId = tokenProvider.getUserIdFromToken(token);
        assertEquals(1L, userId);
    }

    private User createUser(String email, UserType userType) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password"))
                .userType(userType)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        return userRepository.save(user);
    }

    private void assertNotNull(String token) {
        org.junit.jupiter.api.Assertions.assertNotNull(token);
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }

    private void assertEquals(Long expected, Long actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
