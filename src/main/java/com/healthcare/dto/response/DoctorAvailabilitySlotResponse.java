package com.healthcare.dto.response;

import com.healthcare.dto.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilitySlotResponse {
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private BigDecimal consultationFee;
    private BigDecimal averageRating;
    private LocalDate date;
    private List<TimeSlot> availableSlots;
}
