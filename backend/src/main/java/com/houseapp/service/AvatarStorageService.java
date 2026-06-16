package com.houseapp.service;

import com.houseapp.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarStorageService {
  public static final long MAX_SIZE_BYTES = 2L * 1024L * 1024L;
  private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|jpeg|png|webp)$");
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
  private static final Map<String, Set<String>> EXTENSIONS_BY_TYPE = Map.of(
      "image/jpeg", Set.of("jpg", "jpeg"),
      "image/png", Set.of("png"),
      "image/webp", Set.of("webp")
  );

  private final Path avatarsDir;

  public AvatarStorageService(@Value("${app.upload.dir}") String uploadDir) {
    this.avatarsDir = Path.of(uploadDir).toAbsolutePath().normalize().resolve("avatars").normalize();
  }

  public String save(MultipartFile file) {
    validate(file);
    try {
      Files.createDirectories(avatarsDir);
      String extension = extension(file.getOriginalFilename());
      String filename = UUID.randomUUID() + "." + extension;
      Path target = avatarsDir.resolve(filename).normalize();
      if (!target.startsWith(avatarsDir)) {
        throw validation("Invalid avatar filename");
      }
      try (InputStream input = file.getInputStream()) {
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return filename;
    } catch (IOException exception) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", "Could not store avatar file");
    }
  }

  public Resource load(String filename) {
    if (!isSafeFilename(filename)) {
      throw validation("Invalid avatar filename");
    }
    try {
      Path file = avatarsDir.resolve(filename).normalize();
      if (!file.startsWith(avatarsDir) || !Files.isRegularFile(file)) {
        throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Avatar not found");
      }
      Resource resource = new UrlResource(file.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Avatar not found");
      }
      return resource;
    } catch (IOException exception) {
      throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Avatar not found");
    }
  }

  public void delete(String filename) {
    if (!isSafeFilename(filename)) {
      return;
    }
    try {
      Path file = avatarsDir.resolve(filename).normalize();
      if (file.startsWith(avatarsDir)) {
        Files.deleteIfExists(file);
      }
    } catch (IOException ignored) {
      // Avatar cleanup is best-effort; profile state remains authoritative.
    }
  }

  public String avatarUrl(String avatarPath) {
    return isSafeFilename(avatarPath) ? "/api/files/avatars/" + avatarPath : null;
  }

  public String contentType(String filename) {
    String extension = extension(filename);
    return switch (extension) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "webp" -> "image/webp";
      default -> "application/octet-stream";
    };
  }

  private void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw validation("Avatar file is empty");
    }
    if (file.getSize() > MAX_SIZE_BYTES) {
      throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Avatar file is too large");
    }
    String contentType = clean(file.getContentType()).toLowerCase(Locale.ROOT);
    if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw validation("Unsupported avatar file type");
    }
    String extension = extension(file.getOriginalFilename());
    if (!EXTENSIONS_BY_TYPE.getOrDefault(contentType, Set.of()).contains(extension)) {
      throw validation("Avatar file extension does not match content type");
    }
    validateMagicBytes(file, contentType);
  }

  private void validateMagicBytes(MultipartFile file, String contentType) {
    try (InputStream input = file.getInputStream()) {
      byte[] header = input.readNBytes(12);
      boolean valid = switch (contentType) {
        case "image/jpeg" -> header.length >= 3
            && (header[0] & 0xff) == 0xff
            && (header[1] & 0xff) == 0xd8
            && (header[2] & 0xff) == 0xff;
        case "image/png" -> header.length >= 8
            && (header[0] & 0xff) == 0x89
            && header[1] == 0x50
            && header[2] == 0x4e
            && header[3] == 0x47
            && header[4] == 0x0d
            && header[5] == 0x0a
            && header[6] == 0x1a
            && header[7] == 0x0a;
        case "image/webp" -> header.length >= 12
            && header[0] == 0x52
            && header[1] == 0x49
            && header[2] == 0x46
            && header[3] == 0x46
            && header[8] == 0x57
            && header[9] == 0x45
            && header[10] == 0x42
            && header[11] == 0x50;
        default -> false;
      };
      if (!valid) {
        throw validation("Avatar file content is not a supported image");
      }
    } catch (IOException exception) {
      throw validation("Could not read avatar file");
    }
  }

  private boolean isSafeFilename(String filename) {
    return filename != null && SAFE_FILENAME.matcher(filename).matches();
  }

  private String extension(String filename) {
    String value = clean(filename).toLowerCase(Locale.ROOT);
    int dot = value.lastIndexOf('.');
    if (dot < 0 || dot == value.length() - 1) {
      throw validation("Avatar file extension is required");
    }
    return value.substring(dot + 1);
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private ApiException validation(String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
  }
}
