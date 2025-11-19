package com.healthcare.security.oauth2;

import com.healthcare.entity.Patient;
import com.healthcare.entity.User;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Custom OAuth2 User Service
 * Handles user creation/update after OAuth2 authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    /**
     * Process OAuth2 user - create or update user
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setLastLogin(LocalDateTime.now());
            user = userRepository.save(user);
            log.info("Existing user logged in via OAuth2: {}", email);
        } else {
            user = registerNewOAuth2User(email, name);
            log.info("New user registered via OAuth2: {}", email);
        }

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * Register new OAuth2 user as Patient
     */
    private User registerNewOAuth2User(String email, String fullName) {
        // Create User entity
        User newUser = User.builder()
                .email(email)
                .userType(User.UserType.PATIENT)
                .isActive(true)
                .isEmailVerified(true) // OAuth2 providers verify email
                .password("") // No password for OAuth2 users
                .lastLogin(LocalDateTime.now())
                .build();

        newUser = userRepository.save(newUser);

        // Create Patient profile
        String[] nameParts = splitFullName(fullName);
        Patient patient = Patient.builder()
                .user(newUser)
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .build();

        patientRepository.save(patient);

        return newUser;
    }

    /**
     * Split full name into first and last name
     */
    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"Unknown", "User"};
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }
}
