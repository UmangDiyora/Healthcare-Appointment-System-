package com.healthcare.service;

import com.healthcare.annotation.Auditable;
import com.healthcare.config.TwilioConfig;
import com.healthcare.dto.response.VideoRoomResponse;
import com.healthcare.dto.response.VideoTokenResponse;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.AppointmentStatus;
import com.healthcare.entity.AppointmentType;
import com.healthcare.entity.User;
import com.healthcare.exception.InvalidRequestException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.UnauthorizedAccessException;
import com.healthcare.exception.VideoServiceException;
import com.healthcare.repository.AppointmentRepository;
import com.twilio.exception.ApiException;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import com.twilio.rest.video.v1.Room;
import com.twilio.type.InboundCallPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoConsultationService {

    private final AppointmentRepository appointmentRepository;
    private final TwilioConfig twilioConfig;
    private final AuditService auditService;

    @Transactional
    public VideoRoomResponse createRoom(Long appointmentId, User currentUser) {
        // 1. Validate appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 2. Validate user has access
        validateUserAccess(appointment, currentUser);

        // 3. Validate appointment type is VIDEO
        if (appointment.getAppointmentType() != AppointmentType.VIDEO) {
            throw new InvalidRequestException("Appointment is not a video consultation");
        }

        // 4. Check if room already exists
        if (appointment.getTwilioRoomSid() != null && !appointment.getTwilioRoomSid().isEmpty()) {
            return VideoRoomResponse.builder()
                    .roomSid(appointment.getTwilioRoomSid())
                    .roomName("appointment_" + appointmentId)
                    .status("existing")
                    .build();
        }

        // 5. Create Twilio room
        try {
            Room room = Room.creator()
                    .setUniqueName("appointment_" + appointmentId)
                    .setType(Room.RoomType.GROUP)
                    .setMaxParticipants(2)
                    .setRecordParticipantsOnConnect(true)
                    .setStatusCallback("https://your-domain.com/api/video/callback")
                    .create();

            // 6. Save room SID to appointment
            appointment.setTwilioRoomSid(room.getSid());
            appointmentRepository.save(appointment);

            auditService.log(currentUser, "CREATE", "VIDEO_ROOM", appointmentId);

            log.info("Twilio room created: {} for appointment: {}", room.getSid(), appointmentId);

            return VideoRoomResponse.builder()
                    .roomSid(room.getSid())
                    .roomName(room.getUniqueName())
                    .status(room.getStatus().toString())
                    .maxParticipants(room.getMaxParticipants())
                    .recordingEnabled("true")
                    .build();

        } catch (ApiException e) {
            log.error("Failed to create Twilio room for appointment: {}", appointmentId, e);
            throw new VideoServiceException("Failed to create video room: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Twilio room", e);
            throw new VideoServiceException("Video service unavailable", e);
        }
    }

    @Auditable(action = "VIEW", entityType = "VIDEO_TOKEN")
    @Transactional(readOnly = true)
    public VideoTokenResponse generateAccessToken(Long appointmentId, User currentUser) {
        // 1. Validate appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 2. Validate user has access
        validateUserAccess(appointment, currentUser);

        // 3. Validate appointment type is VIDEO
        if (appointment.getAppointmentType() != AppointmentType.VIDEO) {
            throw new InvalidRequestException("Appointment is not a video consultation");
        }

        // 4. Validate appointment status
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED &&
                appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidRequestException("Video consultation not available for " +
                    appointment.getStatus().name().toLowerCase() + " appointments");
        }

        // 5. Create room if it doesn't exist
        String roomSid = appointment.getTwilioRoomSid();
        if (roomSid == null || roomSid.isEmpty()) {
            VideoRoomResponse room = createRoom(appointmentId, currentUser);
            roomSid = room.getRoomSid();
        }

        // 6. Generate access token
        try {
            String identity = currentUser.getId().toString();
            String participantRole = determineParticipantRole(appointment, currentUser);

            AccessToken token = new AccessToken.Builder(
                    twilioConfig.getAccountSid(),
                    twilioConfig.getApiKey(),
                    twilioConfig.getApiSecret()
            ).identity(identity).build();

            VideoGrant grant = new VideoGrant();
            grant.setRoom(roomSid);
            token.addGrant(grant);

            String jwtToken = token.toJwt();

            auditService.log(currentUser, "GENERATE", "VIDEO_TOKEN", appointmentId);

            log.info("Video token generated for user: {} appointment: {}", currentUser.getId(), appointmentId);

            return VideoTokenResponse.builder()
                    .token(jwtToken)
                    .roomSid(roomSid)
                    .roomName("appointment_" + appointmentId)
                    .identity(identity)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .participantRole(participantRole)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate access token for appointment: {}", appointmentId, e);
            throw new VideoServiceException("Failed to generate access token", e);
        }
    }

    @Auditable(action = "UPDATE", entityType = "VIDEO_CONSULTATION")
    @Transactional
    public void endConsultation(Long appointmentId, User currentUser) {
        // 1. Validate appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 2. Only doctor can end consultation
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(currentUser.getId());
        if (!isDoctor) {
            throw new UnauthorizedAccessException("Only the doctor can end the consultation");
        }

        // 3. Validate room exists
        if (appointment.getTwilioRoomSid() == null || appointment.getTwilioRoomSid().isEmpty()) {
            throw new InvalidRequestException("No video room found for this appointment");
        }

        // 4. Complete the Twilio room
        try {
            Room.updater(appointment.getTwilioRoomSid())
                    .setStatus(Room.RoomStatus.COMPLETED)
                    .update();

            log.info("Twilio room completed: {} for appointment: {}",
                    appointment.getTwilioRoomSid(), appointmentId);

        } catch (ApiException e) {
            log.warn("Failed to complete Twilio room (may already be completed): {}", e.getMessage());
            // Continue even if room update fails
        } catch (Exception e) {
            log.error("Unexpected error completing Twilio room", e);
        }

        // 5. Update appointment status to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        auditService.log(currentUser, "COMPLETE", "VIDEO_CONSULTATION", appointmentId);

        log.info("Video consultation ended for appointment: {} by doctor: {}",
                appointmentId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public VideoRoomResponse getRoomStatus(Long appointmentId, User currentUser) {
        // 1. Validate appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // 2. Validate user has access
        validateUserAccess(appointment, currentUser);

        // 3. Check if room exists
        if (appointment.getTwilioRoomSid() == null || appointment.getTwilioRoomSid().isEmpty()) {
            throw new ResourceNotFoundException("No video room found for this appointment");
        }

        // 4. Fetch room status from Twilio
        try {
            Room room = Room.fetcher(appointment.getTwilioRoomSid()).fetch();

            return VideoRoomResponse.builder()
                    .roomSid(room.getSid())
                    .roomName(room.getUniqueName())
                    .status(room.getStatus().toString())
                    .maxParticipants(room.getMaxParticipants())
                    .build();

        } catch (ApiException e) {
            log.error("Failed to fetch room status from Twilio", e);
            throw new VideoServiceException("Failed to get room status", e);
        }
    }

    // Helper methods

    private void validateUserAccess(Appointment appointment, User currentUser) {
        boolean isPatient = appointment.getPatient().getUser().getId().equals(currentUser.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(currentUser.getId());

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedAccessException("Access denied to this video consultation");
        }
    }

    private String determineParticipantRole(Appointment appointment, User currentUser) {
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(currentUser.getId());
        return isDoctor ? "DOCTOR" : "PATIENT";
    }
}
