package org.example.webimagebackend.service;

import lombok.AllArgsConstructor;
import org.example.webimagebackend.persistence.ProfileRepository;
import org.example.webimagebackend.persistence.entity.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProfileService {

    public static final String LOGIN_ERROR_MESSAGE = "username or password is wrong";
    private final ProfileRepository profileRepository;
    private final TokenService tokenService;


    public void createProfile(String username, String password) {
        var hash = hashPassword(password);
        profileRepository.save(
                Profile.builder()
                        .id(null)
                        .username(username)
                        .passwordHash(hash)
                        .build());
    }


    public String login(String username, String password) {
        var profileOpt = profileRepository.findByUsername(username);
        if (profileOpt.isEmpty()) {
            throw new RuntimeException(LOGIN_ERROR_MESSAGE);
        }
        var profile = profileOpt.get();
        var hash = hashPassword(password);
        if (!hash.equals(profile.getPasswordHash())) {
            throw new RuntimeException(LOGIN_ERROR_MESSAGE);
        }
        return tokenService.generateToken(profile.getUsername(), Map.of("username", profile.getUsername()));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
