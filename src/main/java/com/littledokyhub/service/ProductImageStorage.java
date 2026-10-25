package com.littledokyhub.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorage {
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path directory;

    public ProductImageStorage(@Value("${app.upload.products-dir:uploads/products}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty())
            return null;
        if (file.getSize() > MAX_SIZE)
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB.");

        String originalName = file.getOriginalFilename();
        int extensionIndex = originalName == null ? -1 : originalName.lastIndexOf('.');
        if (extensionIndex < 0)
            throw new IllegalArgumentException("Usa una imagen JPG, PNG o WebP.");

        String extension = originalName.substring(extensionIndex).toLowerCase(Locale.ROOT);
        String expectedType = switch (extension) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            default -> throw new IllegalArgumentException("Usa una imagen JPG, PNG o WebP.");
        };
        String contentType = file.getContentType();
        if (contentType == null || !expectedType.equalsIgnoreCase(contentType.split(";", 2)[0].trim()))
            throw new IllegalArgumentException("El tipo de archivo no coincide con la imagen seleccionada.");

        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(12);
            if (!matchesImageSignature(extension, signature))
                throw new IllegalArgumentException("El archivo no contiene una imagen válida.");
        }

        Files.createDirectories(directory);
        String filename = UUID.randomUUID() + extension;
        Path destination = directory.resolve(filename).normalize();
        if (!destination.startsWith(directory))
            throw new IllegalArgumentException("Nombre de archivo inválido.");
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/products/" + filename;
    }

    private boolean matchesImageSignature(String extension, byte[] signature) {
        return switch (extension) {
            case ".jpg", ".jpeg" -> signature.length >= 3
                    && (signature[0] & 0xFF) == 0xFF
                    && (signature[1] & 0xFF) == 0xD8
                    && (signature[2] & 0xFF) == 0xFF;
            case ".png" -> signature.length >= 8
                    && (signature[0] & 0xFF) == 0x89
                    && signature[1] == 0x50
                    && signature[2] == 0x4E
                    && signature[3] == 0x47
                    && signature[4] == 0x0D
                    && signature[5] == 0x0A
                    && signature[6] == 0x1A
                    && signature[7] == 0x0A;
            case ".webp" -> signature.length >= 12
                    && signature[0] == 'R' && signature[1] == 'I' && signature[2] == 'F' && signature[3] == 'F'
                    && signature[8] == 'W' && signature[9] == 'E' && signature[10] == 'B' && signature[11] == 'P';
            default -> false;
        };
    }
}