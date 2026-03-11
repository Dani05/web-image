package org.example.webimagebackend.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.controller.dto.ImageResponse;
import org.example.webimagebackend.model.Image;
import org.example.webimagebackend.service.ImageService;
import org.example.webimagebackend.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@Slf4j
@AllArgsConstructor
public class ImageController {

    private final TokenService tokenService;
    private final ImageService imageService;


    @GetMapping
    public ResponseEntity<List<ImageResponse>> getAllImages() {
        var images = imageService.getAllImagesWithDetails();
        log.info("Getting all images, count: {}", images.size());
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable Long id) {
        return imageService.getAllImagesWithDetails().stream()
                .filter(img -> img.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String imageName,
            @RequestParam("description") String description,
            @RequestHeader("Authorization") String authHeader) {

        if (imageName == null || imageName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (imageName.length() > 40) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Created image with");
        var token = authHeader.replace("Bearer ", "");
        tokenService.verify(token);
        var claims = tokenService.getClaims(token);
        String username;
        String userId;
        try {
            username = claims.get("username").toString();
            userId = claims.get("id").toString();
            if (username == null || username.isEmpty() || userId == null || userId.isEmpty()) {
                throw new RuntimeException("Missing required token claims");
            }
        }
        catch (Exception e){
            log.error("Invalid token claims", e);
            return ResponseEntity.status(401).build();
        }

        imageService.saveImage(file, imageName, description, userId);
        return ResponseEntity.status(201).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id, @RequestBody Image updated) {
        log.info("Updated image with id: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader) {
        var token = authHeader.replace("Bearer ", "");
        tokenService.verify(token);
        var claims = tokenService.getClaims(token);
        String userId;
        try {
            userId = claims.get("id").toString();
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("Missing required token claims");
            }
        } catch (Exception e) {
            log.error("Invalid token claims", e);
            return ResponseEntity.status(401).build();
        }

        var imageOpt = imageService.getImageById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!imageOpt.get().getUserId().equals(userId)) {
            log.warn("User {} attempted to delete image {} owned by {}", userId, id, imageOpt.get().getUserId());
            return ResponseEntity.status(402).build();
        }

        imageService.deleteImage(id);
        log.info("Deleted image with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
