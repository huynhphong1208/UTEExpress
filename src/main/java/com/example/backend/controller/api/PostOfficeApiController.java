package com.example.backend.controller.api;

import com.example.backend.dto.postoffice.*;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.PostOfficeService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/post-office")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "1. Post Office Operations", description = "Phân hệ Bưu cục: Tiếp nhận đơn, đóng kiện, luân chuyển kho và cập nhật trạng thái (UC08, UC09, UC10)")
public class PostOfficeApiController {

    private final PostOfficeService postOfficeService;

    @Operation(
            summary = "UC08: Danh sách đơn hàng chờ tiếp nhận",
            description = "Lấy danh sách các đơn hàng mới tạo (TT01) được phân bổ đến bưu cục gửi chỉ định"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy bưu cục chỉ định"
            )
    })
    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse<List<PendingOrderResponse>>> getPendingOrders(
            @Parameter(description = "Mã bưu cục gửi", example = "KHO01", required = true)
            @RequestParam String maKho) {
        List<PendingOrderResponse> orders = postOfficeService.getPendingOrders(maKho);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng chờ tiếp nhận thành công", orders));
    }

    @Operation(
            summary = "UC08: Tiếp nhận đơn hàng và lập kiện",
            description = "Bưu cục xác nhận tiếp nhận đơn hàng tại quầy, đo đạc kích thước/khối lượng, lập kiện và chuyển trạng thái đơn sang 'Đã tiếp nhận' (TT02)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tiếp nhận đơn hàng thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Trạng thái đơn hàng không hợp lệ hoặc sai bưu cục"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng hoặc bưu cục"
            )
    })
    @PostMapping("/orders/{maDH}/receive")
    public ResponseEntity<ApiResponse<ReceiveOrderResponse>> receiveOrder(
            @Parameter(description = "Mã đơn hàng cần tiếp nhận", example = "DH000001", required = true)
            @PathVariable String maDH,
            @Valid @RequestBody ReceiveOrderRequest request) {
        ReceiveOrderResponse response = postOfficeService.receiveOrder(maDH, request);
        return ResponseEntity.ok(ApiResponse.success(response.getThongBao(), response));
    }

    @Operation(
            summary = "UC09: Chuyển kho cho kiện hàng lưu kho",
            description = "Cập nhật vị trí lưu kho hiện tại của một kiện hàng sang bưu cục / kho trung chuyển mới"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Chuyển kho thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy kiện hàng hoặc bưu cục đích"
            )
    })
    @PutMapping("/packages/{maKien}/transfer-warehouse")
    public ResponseEntity<ApiResponse<TransferWarehouseResponse>> transferWarehouse(
            @Parameter(description = "Mã kiện hàng", example = "KIEN000001", required = true)
            @PathVariable String maKien,
            @Valid @RequestBody TransferWarehouseRequest request) {
        TransferWarehouseResponse response = postOfficeService.transferWarehouse(maKien, request);
        return ResponseEntity.ok(ApiResponse.success(response.getThongBao(), response));
    }

    @Operation(
            summary = "UC10: Cập nhật trạng thái luân chuyển đơn hàng",
            description = "Cập nhật trạng thái luân chuyển đơn hàng theo quy trình nghiêm ngặt và lưu vết vào Lịch sử trạng thái"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật trạng thái thành công"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bước chuyển trạng thái không hợp lệ trong quy trình"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng hoặc mã trạng thái"
            )
    })
    @PostMapping("/orders/{maDH}/status")
    public ResponseEntity<ApiResponse<UpdateOrderStatusResponse>> updateOrderStatus(
            @Parameter(description = "Mã đơn hàng", example = "DH000001", required = true)
            @PathVariable String maDH,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        UpdateOrderStatusResponse response = postOfficeService.updateOrderStatus(maDH, request);
        return ResponseEntity.ok(ApiResponse.success(response.getThongBao(), response));
    }
}
