package com.healthcare.aspect;

import com.healthcare.annotation.Auditable;
import com.healthcare.entity.User;
import com.healthcare.security.UserPrincipal;
import com.healthcare.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for automatic audit logging
 * Intercepts methods annotated with @Auditable
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Execute the method first
        Object result = joinPoint.proceed();

        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found for audit logging");
                return result;
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User currentUser = userPrincipal.getUser();

            // Get HTTP request details
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = getClientIpAddress(request);
            String userAgent = request != null ? request.getHeader("User-Agent") : null;

            // Extract entity ID from result or method parameters
            Long entityId = extractEntityId(result, joinPoint);

            // Build details if requested
            String details = null;
            if (auditable.includeDetails()) {
                details = buildDetails(joinPoint, result);
            }

            // Log the audit entry
            auditService.log(
                    currentUser,
                    auditable.action(),
                    auditable.entityType(),
                    entityId,
                    ipAddress,
                    userAgent,
                    details
            );

        } catch (Exception e) {
            log.error("Error creating audit log", e);
            // Don't fail the operation due to audit logging failure
        }

        return result;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Handle multiple IPs in X-Forwarded-For header
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    private Long extractEntityId(Object result, ProceedingJoinPoint joinPoint) {
        // Try to extract ID from result
        if (result != null) {
            try {
                // Use reflection to get getId() method if it exists
                var idMethod = result.getClass().getMethod("getId");
                Object idValue = idMethod.invoke(result);
                if (idValue instanceof Long) {
                    return (Long) idValue;
                }
            } catch (Exception e) {
                // No getId method or not accessible, try parameters
            }
        }

        // Try to extract ID from method parameters
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals("id") || parameterNames[i].endsWith("Id")) {
                if (args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }

        return null;
    }

    private String buildDetails(ProceedingJoinPoint joinPoint, Object result) {
        try {
            StringBuilder details = new StringBuilder();
            details.append("Method: ").append(joinPoint.getSignature().getName());

            // Add method parameters
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                details.append(", Parameters: ").append(objectMapper.writeValueAsString(args));
            }

            // Add result if not too large
            if (result != null) {
                String resultJson = objectMapper.writeValueAsString(result);
                if (resultJson.length() < 1000) {
                    details.append(", Result: ").append(resultJson);
                }
            }

            return details.toString();
        } catch (Exception e) {
            log.warn("Failed to build audit details", e);
            return null;
        }
    }
}
