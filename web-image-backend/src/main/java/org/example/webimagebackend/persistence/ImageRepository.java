package org.example.webimagebackend.persistence;

import org.example.webimagebackend.persistence.entity.ImageEntity;

import java.util.List;
import java.util.Optional;

public interface ImageRepository {
    ImageEntity save(ImageEntity image);
    Optional<ImageEntity> findById(Long id);
    List<ImageEntity> findAll();
    List<ImageEntity> findByUserId(String userId);
    void deleteById(Long id);
}
