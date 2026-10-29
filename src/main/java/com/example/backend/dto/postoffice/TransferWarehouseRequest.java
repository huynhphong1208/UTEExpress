package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu chuyển kho / bưu cục cho kiện hàng (UC09)")
public class TransferWarehouseRequest {

    @NotBlank(message = "Mã kho mới không được để trống")
    @Schema(description = "Mã bưu cục / kho đích chuyển đến", example = "KHO02")
    private String maKhoMoi;

    @Schema(description = "Ghi chú điều chuyển kho", example = "Chuyển lưu kho sang Bưu cục Cầu Giấy")
    private String ghiChu;

    @Schema(description = "Mã người dùng thực hiện chuyển kho", example = "ND002")
    private String maND;
}
