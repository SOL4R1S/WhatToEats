package com.whattoeat.global.upload;

import com.whattoeat.global.exception.InvalidImageFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("업로드할 파일이 없습니다.");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String ext = getExtension(original).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new InvalidImageFormatException("지원하지 않는 이미지 형식입니다: " + ext);
        }

        Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String stored = UUID.randomUUID() + "." + ext;
        file.transferTo(dir.resolve(stored));

        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        return prefix + stored;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1);
    }
}
