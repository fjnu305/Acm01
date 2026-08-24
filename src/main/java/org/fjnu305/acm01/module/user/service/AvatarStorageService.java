package org.fjnu305.acm01.module.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final UploadProperties uploadProperties;

    @PostConstruct
    void ensureAvatarDir() throws IOException {
        Files.createDirectories(resolveAvatarDir());
    }

    public String storeAvatar(Long userId, MultipartFile file) {
        validate(file);

        String extension = resolveExtension(file);
        String filename = userId + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = resolveAvatarDir().resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.AVATAR_INVALID, "Failed to save avatar");
        }

        String publicPath = uploadProperties.getAvatarPublicPath();
        if (!publicPath.startsWith("/")) {
            publicPath = "/" + publicPath;
        }
        if (publicPath.endsWith("/")) {
            publicPath = publicPath.substring(0, publicPath.length() - 1);
        }
        return publicPath + "/" + filename;
    }

    public void deleteIfLocal(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return;
        }

        String publicPath = uploadProperties.getAvatarPublicPath();
        if (!publicPath.startsWith("/")) {
            publicPath = "/" + publicPath;
        }

        if (!avatarUrl.startsWith(publicPath + "/")) {
            return;
        }

        String filename = avatarUrl.substring(publicPath.length() + 1);
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return;
        }

        Path file = resolveAvatarDir().resolve(filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // ???????????????
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.AVATAR_INVALID, "Avatar file is required");
        }
        if (file.getSize() > uploadProperties.getAvatarMaxBytes()) {
            throw new BusinessException(ErrorCode.AVATAR_INVALID, "Avatar file is too large");
        }

        String contentType = normalizeContentType(file);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.AVATAR_INVALID, "Only JPG, PNG, WEBP and GIF are allowed");
        }
    }

    private String normalizeContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String normalized = contentType.toLowerCase(Locale.ROOT);
            if (ALLOWED_CONTENT_TYPES.contains(normalized)) {
                return normalized;
            }
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return switch (filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "";
        };
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = normalizeContentType(file);
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException(ErrorCode.AVATAR_INVALID, "Unsupported image type");
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new BusinessException(ErrorCode.AVATAR_INVALID, "Unsupported image type");
        };
    }

    private Path resolveAvatarDir() {
        return Path.of(uploadProperties.getAvatarDir()).toAbsolutePath().normalize();
    }
}
