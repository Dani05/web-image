package org.example.webimagebackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.model.Image;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/images")
@Slf4j
public class ImageController {


    @GetMapping
    public ResponseEntity<List<Image>> getAllImages() {
        log.info("Getting all images, count: {}", 0);
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        log.info("Getting image with id: {}", id);
        return ResponseEntity.ok(new Image(id, "Sample Image", "http://example.com/image.jpg", "A sample image description"));
    }

    @PostMapping
    public ResponseEntity<Image> createImage(@RequestBody Image image) {
        log.info("Created image with id: {}", image.getId());
        return ResponseEntity.status(201).body(image);
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
