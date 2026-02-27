package org.example.webimagebackend.persistence;

import org.example.webimagebackend.persistence.entity.Profile;

import java.util.Optional;

public interface ProfileRepository {
    Profile save(Profile profile);
    Optional<Profile> findByUsername(String username);
}
