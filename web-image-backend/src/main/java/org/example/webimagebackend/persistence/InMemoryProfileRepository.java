package org.example.webimagebackend.persistence;

import org.example.webimagebackend.persistence.entity.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProfileRepository implements ProfileRepository {
    private final List<Profile> profiles = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Profile save(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(idCounter.getAndIncrement());
        }
        profiles.add(profile);
        return profile;
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return profiles.stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst();
    }
}
