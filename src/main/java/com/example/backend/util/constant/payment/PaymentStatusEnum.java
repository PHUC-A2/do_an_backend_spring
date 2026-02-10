package com.example.backend.util.constant.payment;

public enum PaymentStatusEnum {
    PENDING, // đã tạo, chưa thanh toán
    PAID, // đã nhận tiền
    CANCELLED // hủy / không thanh toán
}
