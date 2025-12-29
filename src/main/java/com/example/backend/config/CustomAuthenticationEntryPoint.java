package com.example.backend.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.backend.domain.response.common.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @Component // dùng để biến nó thành Bean
// public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

//     private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

//     private final ObjectMapper mapper;

//     public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
//         this.mapper = mapper;
//     }

//     @Override
//     public void commence(HttpServletRequest request, HttpServletResponse response,
//             AuthenticationException authException) throws IOException, ServletException {
//         this.delegate.commence(request, response, authException);
//         response.setContentType("application/json;charset=UTF-8");

//         RestResponse<Object> res = new RestResponse<Object>();
//         res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

//         String errorMessage = Optional.ofNullable(authException.getCause()) // NULL
//                 .map(Throwable::getMessage)
//                 .orElse(authException.getMessage());
//         res.setError(errorMessage);

//         res.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)...");
//         System.out.println(res.getError());
//         System.out.println(res.getMessage());

//         mapper.writeValue(response.getWriter(), res);
//     }

// }

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("Unauthorized");
        res.setMessage("Token không hợp lệ, hết hạn hoặc chưa đăng nhập");

        mapper.writeValue(response.getWriter(), res);
    }
}
