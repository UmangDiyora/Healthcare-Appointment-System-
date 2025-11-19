package com.healthcare.repository;

import com.healthcare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    List<User> findByUserType(User.UserType userType);

    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    List<User> findByIsEmailVerifiedFalse();

    List<User> findByLastLoginBefore(LocalDateTime dateTime);
}
