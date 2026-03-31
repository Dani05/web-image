package org.example.webimagebackend.persistence;

import org.example.webimagebackend.persistence.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByUserId(String userId);
    List<ImageEntity> findTop21ByOrderByUploadedAtDesc();
}
