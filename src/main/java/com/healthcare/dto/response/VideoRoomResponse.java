package com.healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoomResponse {
    private String roomSid;
    private String roomName;
    private String status;
    private Integer maxParticipants;
    private String recordingEnabled;
}
