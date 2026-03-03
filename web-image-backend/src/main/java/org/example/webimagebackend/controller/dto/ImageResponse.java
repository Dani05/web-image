package org.example.webimagebackend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String name;
    private String description;
    private String username;
    private String imageData;  // Base64 data URI: "data:image/jpeg;base64,..."
    private LocalDateTime uploadedAt;
}

