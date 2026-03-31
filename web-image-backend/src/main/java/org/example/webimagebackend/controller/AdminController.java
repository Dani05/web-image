package org.example.webimagebackend.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.service.ImageService;
import org.example.webimagebackend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final ImageService imageService;
    private final ProfileService profileService;

    @DeleteMapping("/purge")
    public ResponseEntity<Void> purgeAllData() {
        imageService.deleteAllImagesAndFiles();
        profileService.deleteAllProfiles();
        log.warn("All images and profiles deleted via purge endpoint");
        return ResponseEntity.noContent().build();
    }
}
