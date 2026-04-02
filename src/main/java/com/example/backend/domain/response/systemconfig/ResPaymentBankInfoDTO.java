package com.example.backend.domain.response.systemconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResPaymentBankInfoDTO {
    private String bankCode;
    private String accountName;
    private String accountNo;
}
