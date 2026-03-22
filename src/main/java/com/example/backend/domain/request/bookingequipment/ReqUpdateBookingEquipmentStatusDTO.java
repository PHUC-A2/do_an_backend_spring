package com.example.backend.domain.request.bookingequipment;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateBookingEquipmentStatusDTO {

    @NotNull(message = "Trạng thái không được để trống")
    private BookingEquipmentStatusEnum status;

    /** Ghi chú biên bản lúc trả (tình trạng thực tế). */
    private String returnConditionNote;

    /**
     * Nếu cả ba trường dưới đây đều null → xử lý theo {@link #status} kiểu cũ (cả dòng).
     * Nếu có ít nhất một trường không null → kiểm đếm chi tiết; tổng phải bằng SL mượn.
     */
    private Integer quantityReturnedGood;
    private Integer quantityLost;
    private Integer quantityDamaged;

    private String borrowerSignName;
    private String staffSignName;

    /** Người trả thực tế (nếu khác người đặt booking). */
    private String returnerName;

    private String returnerPhone;

    /** Người nhận thiết bị phía sân (bắt buộc khi khách ghi nhận trả). */
    private String receiverName;

    private String receiverPhone;

    /** Có in / lưu biên bản trả hay không. */
    private Boolean returnReportPrintOptIn;
}
