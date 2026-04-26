package com.example.backend.domain.response.login;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwtUserDTO { // người dùng trong token
    private Long id;
    private String email;
    private String name;
    /** Tenant đang chọn; dùng cho cô lập dữ liệu admin. */
    private Long tenantId;
    /** Tên gói (PRO, FREE...) khi context là tenant cửa hàng. */
    private String plan;
    /** Quyền hiệu lực theo gói (khớp claim authorities). */
    private List<String> permissions = new ArrayList<>();
}