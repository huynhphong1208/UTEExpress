package com.example.backend.dto.postoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật trạng thái luân chuyển đơn hàng (UC10)")
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Mã trạng thái mới không được để trống")
    @Schema(description = "Mã trạng thái mới (TT01..TT08)", example = "TT03")
    private String maTrangThaiMoi;

    @Schema(description = "Mã người dùng thực hiện cập nhật", example = "ND002")
    private String maND;

    @Schema(description = "Ghi chú cập nhật luân chuyển", example = "Hàng đang xuất kho vận chuyển liên tỉnh")
    private String ghiChu;
}
