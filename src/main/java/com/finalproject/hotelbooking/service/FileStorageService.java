package com.finalproject.hotelbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadPath);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String storedName = UUID.randomUUID() + extension;
        Path destination = uploadPath.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + storedName;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not store uploaded image.");
        }
    }
}
