package org.example.webimagebackend.persistence.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ImageEntity {
    private Long id;
    private String name;
    private String description;
    private String contentType;
    private String filePath;
    private String userId;
    private LocalDateTime uploadedAt;
}

