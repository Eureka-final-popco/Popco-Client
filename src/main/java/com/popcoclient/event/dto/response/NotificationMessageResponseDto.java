package com.popcoclient.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.popcoclient.event.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageResponseDto {
    private String id;
    private String title;
    private String message;
    private String eventId;
    private NotificationType type;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;
    
    private String targetAudience; // "ALL", "SPECIFIC_USERS" 등

}