package com.example.backend.domain.request.account;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateAccountDTO {
    
    @Size(max = 50, message = "Tên tối đa 50 ký tự")
    private String name;

    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^$|^\\d{9,11}$", message = "Số điện thoại phải gồm 9–11 chữ số")
    private String phoneNumber;

    private String avatarUrl;
}
