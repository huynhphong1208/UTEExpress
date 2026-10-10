package com.example.backend.controller.api;

import com.example.backend.dto.payment.*;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "2. Finance & COD Reconciliation", description = "Phân hệ Tài chính: Bảng kê thanh toán, xử lý cước/COD và đối soát COD Admin (UC06, UC07, 4.2.22)")
public class PaymentApiController {

    private final PaymentService paymentService;

    @Operation(
            summary = "UC06: Bảng kê chi tiết thanh toán của đơn hàng",
            description = "Lấy thông tin tổng hợp cước vận chuyển, tiền thu hộ COD, trạng thái đã thanh toán và lịch sử các lần nộp tiền của đơn hàng"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng"
            )
    })
    @GetMapping("/orders/{maDH}/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getOrderPaymentSummary(
            @Parameter(description = "Mã đơn hàng", example = "DH000001", required = true)
            @PathVariable String maDH) {
        PaymentSummaryResponse response = paymentService.getOrderPaymentSummary(maDH);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thanh toán đơn hàng thành công", response));
    }

    @Operation(
            summary = "UC06, UC07: Xử lý giao dịch thanh toán hoặc xác nhận thu COD",
            description = "Xử lý nộp tiền cước vận chuyển hoặc người nhận thanh toán tiền thu hộ COD cho tài xế/bưu cục"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Giao dịch thanh toán thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Khoản thanh toán không hợp lệ hoặc đã được thanh toán trước đó"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng"
            )
    })
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<ProcessPaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success(response.getThongBao(), response));
    }

    @Operation(
            summary = "Mục 4.2.22: Màn hình đối soát COD Admin",
            description = "Admin theo dõi danh sách tất cả các khoản thu hộ COD, tổng cước đã thu, COD chờ đối soát và COD đã hoàn tất chi trả cho người gửi"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách đối soát COD thành công"
            )
    })
    @GetMapping("/admin/cod-reconciliation")
    public ResponseEntity<ApiResponse<CodReconciliationResponse>> getCodReconciliation() {
        CodReconciliationResponse response = paymentService.getCodReconciliation();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu đối soát COD thành công", response));
    }
}
