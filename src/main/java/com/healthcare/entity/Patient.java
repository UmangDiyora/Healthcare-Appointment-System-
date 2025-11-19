package com.healthcare.entity;

import com.healthcare.config.EncryptedLocalDateConverter;
import com.healthcare.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient entity
 * HIPAA Compliant: All PII fields encrypted at rest
 */
@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "first_name", nullable = false, length = 500)
    private String firstName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "last_name", nullable = false, length = 500)
    private String lastName;

    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column(name = "date_of_birth", length = 500)
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String address;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "emergency_contact_name", length = 500)
    private String emergencyContactName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "emergency_contact_phone", length = 500)
    private String emergencyContactPhone;

    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "insurance_number", length = 500)
    private String insuranceNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "medical_history_summary", columnDefinition = "TEXT")
    private String medicalHistorySummary;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
