package com.healthcare.service;

import com.healthcare.dto.request.DoctorRegistrationRequest;
import com.healthcare.dto.request.LoginRequest;
import com.healthcare.dto.request.PatientRegistrationRequest;
import com.healthcare.dto.response.ApiResponse;
import com.healthcare.dto.response.AuthResponse;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Authentication Service
 * Handles user registration and login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Register new patient
     */
    @Transactional
    public ApiResponse registerPatient(PatientRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email is already registered");
        }

        // Create User entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(User.UserType.PATIENT)
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isEmailVerified(false) // Require email verification
                .build();

        user = userRepository.save(user);

        // Create Patient profile
        Patient patient = Patient.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .build();

        patientRepository.save(patient);

        log.info("New patient registered: {}", request.getEmail());

        return ApiResponse.success("Patient registered successfully. Please verify your email.");
    }

    /**
     * Register new doctor (requires admin approval)
     */
    @Transactional
    public ApiResponse registerDoctor(DoctorRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email is already registered");
        }

        // Check if license number already exists
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            return ApiResponse.error("License number is already registered");
        }

        // Create User entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(User.UserType.DOCTOR)
                .phoneNumber(request.getPhoneNumber())
                .isActive(false) // Require admin approval
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);

        // Create Doctor profile
        Doctor doctor = Doctor.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .yearsOfExperience(request.getYearsOfExperience())
                .qualification(request.getQualification())
                .bio(request.getBio())
                .consultationFee(request.getConsultationFee())
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        doctorRepository.save(doctor);

        log.info("New doctor registered (pending approval): {}", request.getEmail());

        return ApiResponse.success("Doctor registration submitted. Awaiting admin approval.");
    }

    /**
     * Login user and return JWT token
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);

            // Update last login
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Log successful authentication
            auditService.logAuthentication(request.getEmail(), true, ipAddress, userAgent);

            log.info("User logged in successfully: {}", request.getEmail());

            return AuthResponse.of(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getUserType().name(),
                    jwtExpirationMs / 1000 // Convert to seconds
            );

        } catch (Exception e) {
            // Log failed authentication
            auditService.logAuthentication(request.getEmail(), false, ipAddress, userAgent);

            log.error("Login failed for email: {}", request.getEmail());
            throw e;
        }
    }
}
