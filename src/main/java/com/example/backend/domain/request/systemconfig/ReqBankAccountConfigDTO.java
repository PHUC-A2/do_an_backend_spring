package com.example.backend.domain.request.systemconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqBankAccountConfigDTO {

    @NotBlank(message = "Mã ngân hàng không được để trống")
    private String bankCode;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNo;

    @NotBlank(message = "Tên tài khoản không được để trống")
    private String accountName;

    private Boolean active;
}
