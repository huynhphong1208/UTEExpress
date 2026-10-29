package com.example.backend.controller.api;

import com.example.backend.dto.report.AdminOverviewReportResponse;
import com.example.backend.dto.report.SlaPerformanceReportResponse;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "3. Admin Reports & Statistics", description = "Phân hệ Thống kê & Báo cáo: Tổng quan hệ thống, tài chính và chỉ số chất lượng dịch vụ SLA (UC17, 4.2.23)")
public class AdminReportApiController {

    private final AdminReportService adminReportService;

    @Operation(
            summary = "UC17, Mục 4.2.17: Thống kê tổng quan Admin Dashboard",
            description = "Báo cáo tổng quan số lượng đơn hàng theo từng trạng thái (đang VC, hoàn thành, hủy/trả), tổng doanh thu cước phí và tổng tiền COD"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy báo cáo tổng quan thành công"
            )
    })
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewReportResponse>> getOverviewReport() {
        AdminOverviewReportResponse response = adminReportService.getOverviewReport();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo tổng quan Admin Dashboard thành công", response));
    }

    @Operation(
            summary = "Mục 4.2.23: Báo cáo chỉ số chất lượng dịch vụ SLA & Hiệu suất bưu cục",
            description = "Đo lường tỷ lệ giao thành công, tỷ lệ đúng cam kết SLA và xếp hạng top các bưu cục có lưu lượng xử lý cao nhất"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy báo cáo SLA và hiệu suất bưu cục thành công"
            )
    })
    @GetMapping("/sla-performance")
    public ResponseEntity<ApiResponse<SlaPerformanceReportResponse>> getSlaPerformanceReport() {
        SlaPerformanceReportResponse response = adminReportService.getSlaPerformanceReport();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo SLA và hiệu suất bưu cục thành công", response));
    }
}
