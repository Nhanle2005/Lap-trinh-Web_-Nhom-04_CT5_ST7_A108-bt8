package nhanle.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

  private Path rootDir; 

  public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
    Path desired = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(desired);
      this.rootDir = desired;
    } catch (IOException e) {
      Path fallback = Paths.get("./uploads").toAbsolutePath().normalize();
      try {
        Files.createDirectories(fallback);
      } catch (IOException ex) {
        throw new IllegalStateException(
            "Không tạo được thư mục upload ở: " + desired + " và fallback: " + fallback, ex);
      }
      this.rootDir = fallback;
      System.err.println("[WARN] Upload dir '" + desired + "' không dùng được. Fallback: " + fallback);
    }
  }

  public String storeImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) return null;

    String origin = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
    if (origin.contains("..")) throw new IOException("Tên file không hợp lệ: " + origin);

    String ext = "";
    int dot = origin.lastIndexOf('.');
    if (dot >= 0) ext = origin.substring(dot);

    String newName = UUID.randomUUID().toString().replace("-", "") + ext.toLowerCase();
    Path target = rootDir.resolve(newName).normalize();
    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    return newName;
  }

  public void deleteIfExists(String filename) {
    if (filename == null || filename.isBlank()) return;
    try { Files.deleteIfExists(rootDir.resolve(filename).normalize()); } catch (Exception ignored) {}
  }
}
