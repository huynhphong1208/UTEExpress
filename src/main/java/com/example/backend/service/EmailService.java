package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service gửi email chứa OTP (HTML template)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Gửi email OTP đăng ký không đồng bộ
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "UTEExpress Logistics");
            helper.setTo(toEmail);
            helper.setSubject("UTEExpress - Mã xác thực OTP của bạn");
            helper.setText(buildOtpEmailHtml(otp, purpose), true);

            mailSender.send(message);
            log.info("Đã gửi OTP đến email: {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Lỗi gửi email đến {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    private String buildOtpEmailHtml(String otp, String purpose) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
                        .container { max-width: 480px; margin: 0 auto; background: #fff;
                                     border-radius: 12px; overflow: hidden;
                                     box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #1a237e, #283593);
                                  padding: 32px; text-align: center; color: white; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .header p { margin: 4px 0 0; opacity: 0.85; font-size: 14px; }
                        .body { padding: 32px; text-align: center; }
                        .body p { color: #555; font-size: 15px; }
                        .otp-box { display: inline-block; background: #f0f4ff;
                                   border: 2px dashed #3f51b5; border-radius: 12px;
                                   padding: 16px 40px; margin: 24px 0; }
                        .otp-code { font-size: 40px; font-weight: bold; letter-spacing: 12px;
                                    color: #1a237e; }
                        .expiry { color: #e53935; font-size: 13px; margin-top: 8px; }
                        .footer { background: #f9f9f9; padding: 16px; text-align: center;
                                  font-size: 12px; color: #999; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📦 UTEExpress</h1>
                            <p>Hệ thống Logistics thông minh</p>
                        </div>
                        <div class="body">
                            <p>Xin chào! Bạn yêu cầu mã OTP để <strong>%s</strong>.</p>
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                            </div>
                            <p class="expiry">⏳ Mã OTP có hiệu lực trong <strong>5 phút</strong>.</p>
                            <p style="color: #888; font-size: 13px;">Nếu bạn không thực hiện yêu cầu này,
                            vui lòng bỏ qua email này.</p>
                        </div>
                        <div class="footer">
                            © 2024 UTEExpress Logistics. Không trả lời email này.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(purpose, otp);
    }
}
