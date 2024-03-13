package com.pkozlowski.webstore.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UserErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
