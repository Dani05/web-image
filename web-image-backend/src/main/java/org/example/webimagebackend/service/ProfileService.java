package org.example.webimagebackend.service;

import lombok.AllArgsConstructor;
import org.example.webimagebackend.persistence.ProfileRepository;
import org.example.webimagebackend.persistence.entity.Profile;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileService {

    public static final String LOGIN_ERROR_MESSAGE = "username or password is wrong";
    private final ProfileRepository profileRepository;

    public void createProfile(String username, String password) {
        var hash = hashPassword(password);
        profileRepository.save(
                Profile.builder()
                        .id(null)
                        .username(username)
                        .passwordHash(hash)
                        .build());
    }


    public String login(String username, String hash) {
        var profileOpt = profileRepository.findByUsername(username);
        if (profileOpt.isEmpty()) {
            throw new RuntimeException(LOGIN_ERROR_MESSAGE);
        }
        var profile = profileOpt.get();
        if (!hash.equals(profile.getPasswordHash())) {
            throw new RuntimeException(LOGIN_ERROR_MESSAGE);
        }
        return "token";
    }


    private String hashPassword(String password) {
        //TODO
        return "hash";
    }
}
