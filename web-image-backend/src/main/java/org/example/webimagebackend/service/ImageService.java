package org.example.webimagebackend.service;

import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.controller.dto.ImageResponse;
import org.example.webimagebackend.persistence.ImageRepository;
import org.example.webimagebackend.persistence.ProfileRepository;
import org.example.webimagebackend.persistence.entity.ImageEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final ProfileRepository profileRepository;
    private final Path uploadDir;

    public ImageService(ImageRepository imageRepository,
                        ProfileRepository profileRepository,
                        @Value("${app.upload.dir:/uploads}") String uploadDirPath) {
        this.imageRepository = imageRepository;
        this.profileRepository = profileRepository;
        this.uploadDir = Paths.get(uploadDirPath);
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Upload directory: {}", this.uploadDir.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDirPath, e);
        }
    }

    public void saveImage(MultipartFile file, String name, String description, String userId) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = uploadDir.resolve(filename);
            file.transferTo(destination);

            ImageEntity entity = ImageEntity.builder()
                    .name(name)
                    .description(description)
                    .contentType(file.getContentType())
                    .filePath(destination.toString())
                    .userId(userId)
                    .build();
            imageRepository.save(entity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file", e);
        }
    }

    public byte[] getImageData(Long id) throws IOException {
        ImageEntity entity = imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found: " + id));
        return Files.readAllBytes(Paths.get(entity.getFilePath()));
    }

    public List<ImageEntity> getAllImages() {
        return imageRepository.findAll();
    }

    public List<ImageResponse> getAllImagesWithDetails() {
        return imageRepository.findAll().stream()
                .map(img -> {
                    String username = "unknown";
                    try {
                        Long userId = Long.parseLong(img.getUserId());
                        username = profileRepository.findById(userId)
                                .map(p -> p.getUsername())
                                .orElse("unknown");
                    } catch (NumberFormatException ignored) {}

                    String imageData = null;
                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(img.getFilePath()));
                        String mime = img.getContentType() != null ? img.getContentType() : "image/jpeg";
                        imageData = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
                    } catch (IOException e) {
                        log.warn("Could not read file for image {}: {}", img.getId(), e.getMessage());
                    }

                    return new ImageResponse(
                            img.getId(),
                            img.getName(),
                            img.getDescription(),
                            username,
                            imageData
                    );
                })
                .toList();
    }

    public Optional<ImageEntity> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    public List<ImageEntity> getImagesByUserId(String userId) {
        return imageRepository.findByUserId(userId);
    }

    public void deleteImage(Long id) {
        imageRepository.findById(id).ifPresent(entity -> {
            try {
                Files.deleteIfExists(Paths.get(entity.getFilePath()));
            } catch (IOException e) {
                log.warn("Could not delete file for image {}: {}", id, e.getMessage());
            }
        });
        imageRepository.deleteById(id);
    }
}


