package edu.anurag.auconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/** Stores uploaded photos on disk under random names. Only real images are accepted. */
@Service
public class FileStorageService {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Saves the image and returns its stored filename, or null when no file was provided. */
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String ext = EXTENSIONS.get(file.getContentType());
        if (ext == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, WEBP or GIF images are allowed");
        }
        try {
            byte[] head = new byte[12];
            try (InputStream in = file.getInputStream()) {
                if (in.readNBytes(head, 0, head.length) < head.length || !looksLikeImage(head)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "That file is not a valid image");
                }
            }
            String name = UUID.randomUUID() + ext;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, root.resolve(name));
            }
            return name;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the image");
        }
    }

    private static boolean looksLikeImage(byte[] b) {
        boolean jpeg = (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean png = (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        boolean gif = b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
        boolean webp = b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        return jpeg || png || gif || webp;
    }
}
