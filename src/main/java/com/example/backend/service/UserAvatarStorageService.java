package com.example.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service lưu trữ và quản lý ảnh đại diện người dùng trên hệ thống tệp cục bộ (Local File Storage).
 * Hoàn toàn KHÔNG cần thêm cột avatar vào cơ sở dữ liệu PostgreSQL.
 */
@Slf4j
@Service
public class UserAvatarStorageService {

    private final Path storageDir = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");
    private final Path mappingFile = storageDir.resolve("avatar_mapping.json");
    private final Map<String, String> avatarMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            if (Files.exists(mappingFile)) {
                Map<String, String> loaded = objectMapper.readValue(
                        mappingFile.toFile(),
                        new TypeReference<Map<String, String>>() {}
                );
                if (loaded != null) {
                    avatarMap.putAll(loaded);
                }
            }
            log.info("Khởi tạo UserAvatarStorageService thành công với {} avatar", avatarMap.size());
        } catch (Exception e) {
            log.warn("Không thể tải avatar_mapping.json: {}", e.getMessage());
        }
    }

    /**
     * Lấy đường dẫn avatar của người dùng theo mã người dùng
     */
    public String getAvatar(String maNd) {
        if (maNd == null) return null;
        return avatarMap.get(maNd);
    }

    /**
     * Lưu file ảnh tải lên từ máy tính
     */
    public synchronized String saveFile(String maNd, MultipartFile file) throws IOException {
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        String originalFilename = file.getOriginalFilename();
        String ext = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        String filename = "avatar_" + maNd + "_" + System.currentTimeMillis() + ext;
        Path targetPath = storageDir.resolve(filename);
        file.transferTo(targetPath.toFile());

        String avatarUrl = "/uploads/avatars/" + filename;
        avatarMap.put(maNd, avatarUrl);
        persistMapping();
        return avatarUrl;
    }

    /**
     * Lưu đường dẫn link ảnh trực tiếp
     */
    public synchronized String saveLink(String maNd, String link) {
        avatarMap.put(maNd, link);
        persistMapping();
        return link;
    }

    /**
     * Xóa ảnh đại diện của người dùng (trở về mặc định)
     */
    public synchronized void removeAvatar(String maNd) {
        String existing = avatarMap.remove(maNd);
        if (existing != null && existing.startsWith("/uploads/avatars/")) {
            try {
                String filename = existing.substring("/uploads/avatars/".length());
                Files.deleteIfExists(storageDir.resolve(filename));
            } catch (Exception ignored) {}
        }
        persistMapping();
    }

    private void persistMapping() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingFile.toFile(), avatarMap);
        } catch (Exception e) {
            log.error("Lỗi khi lưu avatar_mapping.json: {}", e.getMessage());
        }
    }
}
