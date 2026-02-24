package org.example.webimagebackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {

    @PostMapping("/register")
    public ResponseEntity<String> register() {
        log.info("Registration endpoint called");
        return ResponseEntity.ok("Registration received");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        log.info("Login endpoint called");
        return ResponseEntity.ok("Login received");
    }
}
