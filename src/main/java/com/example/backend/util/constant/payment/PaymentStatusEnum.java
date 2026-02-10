package com.example.backend.util.constant.payment;

public enum PaymentStatusEnum {
    PENDING, // đã tạo, chưa thanh toán ,chờ thanh toán
    PAID, // // admin đã xác nhận đã nhận tiền
    CANCELLED // hủy / không thanh toán
}
