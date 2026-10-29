package com.example.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO chuẩn hóa kết quả phân trang ở tầng Backend.
 * Tương thích tốt với UI (trang tính từ 1, kích thước 5, 10, 20 dòng).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * Danh sách dữ liệu của trang hiện tại
     */
    private List<T> items;

    /**
     * Số thứ tự trang hiện tại (bắt đầu từ 1 cho thân thiện với giao diện người dùng)
     */
    private int page;

    /**
     * Số bản ghi trên mỗi trang (mặc định 5, 10, 20)
     */
    private int size;

    /**
     * Tổng số bản ghi thỏa điều kiện
     */
    private long totalItems;

    /**
     * Tổng số trang
     */
    private int totalPages;

    /**
     * Có trang tiếp theo hay không
     */
    private boolean hasNext;

    /**
     * Có trang trước đó hay không
     */
    private boolean hasPrevious;

    /**
     * Chuyển đổi từ Spring Data Page<T> sang PageResponse<T>
     */
    public static <T> PageResponse<T> from(Page<T> springPage) {
        if (springPage == null) {
            return PageResponse.<T>builder()
                    .items(List.of())
                    .page(1)
                    .size(5)
                    .totalItems(0)
                    .totalPages(1)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }

        int curPage = springPage.getNumber() + 1; // Spring Data page 0-indexed -> 1-indexed
        int totalPages = Math.max(1, springPage.getTotalPages());

        return PageResponse.<T>builder()
                .items(springPage.getContent())
                .page(curPage)
                .size(springPage.getSize())
                .totalItems(springPage.getTotalElements())
                .totalPages(totalPages)
                .hasNext(springPage.hasNext())
                .hasPrevious(springPage.hasPrevious())
                .build();
    }

    /**
     * Phân trang trên bộ nhớ (In-memory pagination) cho các danh sách DTO tổng hợp
     */
    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        if (allItems == null) allItems = List.of();
        if (size <= 0) size = 5;
        if (page <= 0) page = 1;

        int total = allItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<T> pageItems = (fromIndex < total && fromIndex >= 0)
                ? allItems.subList(fromIndex, toIndex)
                : List.of();

        return PageResponse.<T>builder()
                .items(pageItems)
                .page(page)
                .size(size)
                .totalItems(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }
}
