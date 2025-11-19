package com.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Healthcare Appointment System
 * HIPAA-Compliant Spring Boot Application
 *
 * @author Healthcare Team
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class HealthcareAppointmentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcareAppointmentSystemApplication.class, args);
    }
}
