package com.healthcare.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that should be audited
 * Used for HIPAA compliance tracking
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Action being performed (VIEW, CREATE, UPDATE, DELETE, etc.)
     */
    String action();

    /**
     * Entity type being accessed (PATIENT, APPOINTMENT, MEDICAL_RECORD, etc.)
     */
    String entityType();

    /**
     * Whether to include request/response details in audit log
     */
    boolean includeDetails() default false;
}
