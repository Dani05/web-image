package org.example.webimagebackend.service;

import lombok.AllArgsConstructor;
import org.example.webimagebackend.persistence.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ImageService {

    //private final ImageRepository imageRepository;

    public void saveImage(MultipartFile file, String name, String description, String userId) {

    }
}
