package com.example.backend.service;

import com.example.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý OTP trong bộ nhớ với thời hạn hết hạn.
 * Key = "email" hoặc "sdt", Value = {otp, expiry}
 */
@Slf4j
@Service
public class OtpCacheService {

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    private record OtpEntry(String otp, LocalDateTime expiry) {}

    // ConcurrentHashMap thread-safe - key = phone/email
    private final Map<String, OtpEntry> cache = new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên và lưu vào cache
     */
    public String generateAndStore(String key) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        cache.put(key, new OtpEntry(otp, expiry));
        log.debug("OTP sinh cho key={}: {} (hết hạn lúc {})", key, otp, expiry);
        return otp;
    }

    /**
     * Xác thực OTP. Ném BusinessException nếu sai hoặc hết hạn.
     */
    public void verifyAndConsume(String key, String inputOtp) {
        OtpEntry entry = cache.get(key);

        if (entry == null) {
            throw new BusinessException("OTP không tồn tại hoặc đã được sử dụng. Vui lòng gửi lại.");
        }

        if (LocalDateTime.now().isAfter(entry.expiry())) {
            cache.remove(key);
            throw new BusinessException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        if (!entry.otp().equals(inputOtp)) {
            throw new BusinessException("Mã OTP không chính xác.");
        }

        // Xóa OTP sau khi dùng (single-use)
        cache.remove(key);
        log.debug("OTP cho key={} đã xác thực thành công.", key);
    }

    /**
     * Xóa OTP khỏi cache (khi hủy yêu cầu)
     */
    public void invalidate(String key) {
        cache.remove(key);
    }
}
