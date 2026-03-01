package org.example.webimagebackend.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webimagebackend.controller.dto.RegisterRequest;
import org.example.webimagebackend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "https://https://web-image-frontend-bartadani-dev.apps.rm3.7wse.p1.openshiftapps.com")
@RestController
@RequestMapping("/api/profile")
@Slf4j
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        log.info("REGISTRATION ENDPOINT CALLED: {}", request.getUsername());
        try {
            profileService.createProfile(request.getUsername(), request.getPassword());
            return ResponseEntity.status(200).body("Registration received");
        }
        catch (RuntimeException e) {
            log.error("REGISTRATION FAILED username: {} already exists", request.getUsername());
            return ResponseEntity.status(400).body("Username already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody RegisterRequest request) {
        log.info("LOGIN ENDPOINT CALLED");
        try {
            var token = profileService.login(request.getUsername(), request.getPassword());
            log.info("LOGIN SUCCESSFUL username: {}", request.getUsername());
            return ResponseEntity.ok(token);
        }
        catch (RuntimeException e) {
            log.error("LOGIN FAILED username: {}", request.getUsername());
            return ResponseEntity.status(401).body(e.getMessage());
        }

    }
}
