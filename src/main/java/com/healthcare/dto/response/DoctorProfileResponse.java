package com.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String qualification;
    private String bio;
    private BigDecimal consultationFee;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Boolean isActive;
}
