package com.example.backend.service;

import com.example.backend.entity.Kho;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tích hợp Gemini AI để tự động phân tích địa chỉ và chọn bưu cục tối ưu nhất.
 */
@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseRecommendation {
        private String maKhoGui;
        private String maKhoNhan;
        private String lyDo;
        private boolean usedAi;
    }

    /**
     * Xác định kho gửi và kho nhận phù hợp nhất dựa trên địa chỉ lấy và giao.
     */
    public WarehouseRecommendation determineWarehouses(String diaChiLay, String diaChiGiao, List<Kho> danhSachKho) {
        if (danhSachKho == null || danhSachKho.isEmpty()) {
            return WarehouseRecommendation.builder()
                    .maKhoGui("KHO01")
                    .maKhoNhan("KHO02")
                    .lyDo("Không có danh sách kho, dùng kho mặc định")
                    .usedAi(false)
                    .build();
        }

        // Nếu chưa cấu hình API Key, sử dụng thuật toán dự phòng thông minh (fallback)
        if (apiKey == null || apiKey.trim().isBlank()) {
            log.info("Gemini API Key đang để trống trong application.properties. Sử dụng bộ phân tích địa chỉ nội bộ (Fallback).");
            return fallbackWarehouseSelection(diaChiLay, diaChiGiao, danhSachKho, "API Key chưa cấu hình");
        }

        try {
            // Chuẩn bị danh sách kho cho prompt
            StringBuilder khoListBuilder = new StringBuilder();
            for (Kho k : danhSachKho) {
                khoListBuilder.append("- Mã: ").append(k.getMaKho())
                        .append(" | Tên: ").append(k.getTenKho())
                        .append(" | Địa chỉ: ").append(k.getDiaChi())
                        .append("\n");
            }

            String prompt = String.format("""
                    Bạn là hệ thống AI điều phối vận chuyển thông minh của UTE Express tại Việt Nam.
                    Dưới đây là danh sách các bưu cục / kho hiện có trong hệ thống:
                    %s
                    
                    Khách hàng vừa tạo một đơn gửi hàng với thông tin địa chỉ sau:
                    - Địa chỉ lấy hàng (Người gửi): "%s"
                    - Địa chỉ giao hàng (Người nhận): "%s"
                    
                    Nhiệm vụ:
                    1. Phân tích địa chỉ lấy hàng và CHỌN kho gần nhất / cùng quận / cùng tỉnh/thành phố với địa chỉ lấy hàng làm 'maKhoGui'.
                    2. Phân tích địa chỉ giao hàng và CHỌN kho gần nhất / cùng quận / cùng tỉnh/thành phố với địa chỉ giao hàng làm 'maKhoNhan'.
                    
                    YÊU CẦU BẮT BUỘC:
                    - 'maKhoGui' và 'maKhoNhan' PHẢI là một trong các Mã kho có trong danh sách trên (ví dụ: KHO01, KHO02, KHO03, ...). Tuyệt đối không tự bịa ra mã kho.
                    - Trả về DUY NHẤT một chuỗi JSON theo mẫu, không có định dạng markdown ```json hay văn bản giải thích thừa:
                    {"maKhoGui": "KHOxx", "maKhoNhan": "KHOyy", "lyDo": "Ngắn gọn lý do chọn"}
                    """, khoListBuilder.toString(), diaChiLay, diaChiGiao);

            // Xây dựng request gửi Gemini
            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> genConfig = Map.of("response_mime_type", "application/json");
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", genConfig
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String requestUrl = apiUrl + (apiUrl.contains("?") ? "&key=" : "?key=") + apiKey.trim();
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    String responseText = textNode.asText().trim();

                    // Làm sạch chuỗi JSON nếu có bao markdown
                    if (responseText.startsWith("```json")) {
                        responseText = responseText.substring(7);
                    }
                    if (responseText.startsWith("```")) {
                        responseText = responseText.substring(3);
                    }
                    if (responseText.endsWith("```")) {
                        responseText = responseText.substring(0, responseText.length() - 3);
                    }
                    responseText = responseText.trim();

                    JsonNode resultJson = objectMapper.readTree(responseText);
                    String maKhoGui = resultJson.path("maKhoGui").asText();
                    String maKhoNhan = resultJson.path("maKhoNhan").asText();
                    String lyDo = resultJson.path("lyDo").asText("Được đề xuất bởi Gemini AI");

                    Set<String> validKhoCodes = danhSachKho.stream().map(Kho::getMaKho).collect(Collectors.toSet());
                    if (validKhoCodes.contains(maKhoGui) && validKhoCodes.contains(maKhoNhan)) {
                        log.info("Gemini AI chọn kho thành công: gửi={}, nhận={}, lý do={}", maKhoGui, maKhoNhan, lyDo);
                        return WarehouseRecommendation.builder()
                                .maKhoGui(maKhoGui)
                                .maKhoNhan(maKhoNhan)
                                .lyDo(lyDo)
                                .usedAi(true)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể gọi Gemini API ({}: {}). Tự động kích hoạt bộ dự phòng.", e.getClass().getSimpleName(), e.getMessage());
        }

        return fallbackWarehouseSelection(diaChiLay, diaChiGiao, danhSachKho, "Fallback theo quy tắc từ khóa địa phương");
    }

    /**
     * Thuật toán dự phòng dựa trên phân tích từ khóa địa lý (quận, huyện, tỉnh, thành).
     */
    private WarehouseRecommendation fallbackWarehouseSelection(String diaChiLay, String diaChiGiao, List<Kho> danhSachKho, String reasonPrefix) {
        String maKhoGui = findBestMatchingKho(diaChiLay, danhSachKho, "KHO01");
        String maKhoNhan = findBestMatchingKho(diaChiGiao, danhSachKho, "KHO02");

        return WarehouseRecommendation.builder()
                .maKhoGui(maKhoGui)
                .maKhoNhan(maKhoNhan)
                .lyDo(reasonPrefix + " (Kho gửi: " + maKhoGui + ", Kho nhận: " + maKhoNhan + ")")
                .usedAi(false)
                .build();
    }

    private String findBestMatchingKho(String address, List<Kho> danhSachKho, String defaultKho) {
        if (address == null || address.isBlank()) {
            return defaultKho;
        }
        String cleanAddress = address.toLowerCase();

        // 1. Tìm kho có địa chỉ trùng khớp từ khóa cao nhất
        Kho bestKho = null;
        int maxScore = 0;

        for (Kho k : danhSachKho) {
            String khoAddress = (k.getDiaChi() + " " + k.getTenKho()).toLowerCase();
            int score = 0;

            // Kiểm tra các từ khóa hành chính quan trọng
            String[] keywords = {"thủ đức", "quận 1", "quận 5", "quận 3", "bình thạnh", "tân bình", "hồ chí minh", "tp.hcm", "tphcm", "sài gòn",
                    "cầu giấy", "đống đa", "hoàn kiếm", "hà nội", "hải phòng", "đà nẵng", "hải châu", "sơn trà",
                    "cần thơ", "ninh kiều", "biên hòa", "đồng nai", "nha trang", "khánh hòa", "buôn ma thuột", "đắk lắk"};

            for (String kw : keywords) {
                if (cleanAddress.contains(kw) && khoAddress.contains(kw)) {
                    score += kw.length() >= 7 ? 3 : 2;
                }
            }

            if (score > maxScore) {
                maxScore = score;
                bestKho = k;
            }
        }

        if (bestKho != null && maxScore > 0) {
            return bestKho.getMaKho();
        }

        // 2. Nếu không khớp từ khóa đặc biệt, kiểm tra xem defaultKho có trong danh sách không
        boolean hasDefault = danhSachKho.stream().anyMatch(k -> k.getMaKho().equalsIgnoreCase(defaultKho));
        if (hasDefault) {
            return defaultKho;
        }

        return danhSachKho.get(0).getMaKho();
    }
}
