package com.taller.security.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private static final long MAX_IMAGE_BYTES = 15L * 1024L * 1024L;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final Path root;

    public FileStorageService(@Value("${app.storage.upload-dir:../storage/uploads}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String saveWorkshopPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return saveImage(file, "workshops", "No se pudo guardar la foto del taller");
    }

    public String saveRequiredCustomerPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La foto del cliente es obligatoria");
        }
        return saveImage(file, "customers", "No se pudo guardar la foto del cliente");
    }

    public String saveOptionalCustomerPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return saveImage(file, "customers", "No se pudo guardar la foto del cliente");
    }

    private String saveImage(MultipartFile file, String folder, String errorMessage) {
        validateImage(file);
        try {
            Path targetDir = root.resolve(folder).normalize();
            Files.createDirectories(targetDir);
            String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
            String filename = UUID.randomUUID() + extension;
            Path target = targetDir.resolve(filename).normalize();
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return root.relativize(target).toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("La foto debe ser una imagen valida");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("La foto no debe superar 15 MB");
        }
    }

    private String resolveExtension(String originalName, String contentType) {
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return ".jpg";
        if (name.endsWith(".png")) return ".png";
        if (name.endsWith(".webp")) return ".webp";
        if (name.endsWith(".gif")) return ".gif";
        return switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
