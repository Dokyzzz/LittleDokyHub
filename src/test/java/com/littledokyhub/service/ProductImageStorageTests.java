package com.littledokyhub.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ProductImageStorageTests {
    @TempDir
    Path temporaryDirectory;

    @Test
    void storesValidPngWithGeneratedName() throws Exception {
        byte[] imageBytes = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4
        };
        MockMultipartFile image = new MockMultipartFile("imagen", "pizza.png", "image/png", imageBytes);

        String publicPath = new ProductImageStorage(temporaryDirectory.toString()).store(image);

        assertTrue(publicPath.matches("/uploads/products/[0-9a-f-]+\\.png"));
        Path storedFile = temporaryDirectory.resolve(Path.of(publicPath).getFileName());
        assertArrayEquals(imageBytes, Files.readAllBytes(storedFile));
    }

    @Test
    void rejectsFileWithImageExtensionButInvalidSignature() {
        MockMultipartFile image = new MockMultipartFile("imagen", "pizza.png", "image/png", "not an image".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> new ProductImageStorage(temporaryDirectory.toString()).store(image));
    }
}