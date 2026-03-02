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
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestHeader("Authorization") String authHeader) {
        log.info("Created image with name: {}", name);
        var token = authHeader.replace("Bearer ", "");
        tokenService.verify(token);
        var claims = tokenService.getClaims(token);
        imageService.saveImage(file, name, description, "123");
        return ResponseEntity.status(201).build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id, @RequestBody Image updated) {
        log.info("Updated image with id: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        log.info("Deleted image with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
