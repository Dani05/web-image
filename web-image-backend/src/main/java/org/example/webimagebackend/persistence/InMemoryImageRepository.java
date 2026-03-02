package org.example.webimagebackend.persistence;

import org.example.webimagebackend.persistence.entity.ImageEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryImageRepository implements ImageRepository {

    private final Map<Long, ImageEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public ImageEntity save(ImageEntity image) {
        if (image.getId() == null) {
            image.setId(idCounter.getAndIncrement());
        }
        store.put(image.getId(), image);
        return image;
    }

    @Override
    public Optional<ImageEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ImageEntity> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<ImageEntity> findByUserId(String userId) {
        return store.values().stream()
                .filter(img -> userId.equals(img.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}

