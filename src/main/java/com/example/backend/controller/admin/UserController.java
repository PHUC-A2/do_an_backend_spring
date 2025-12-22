package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.user.ReqCreateUserDTO;
import com.example.backend.domain.request.user.ReqUpdateUserDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.user.ResCreateUserDTO;
import com.example.backend.domain.response.user.ResUpdateUserDTO;
import com.example.backend.domain.response.user.ResUserDTO;
import com.example.backend.service.UserService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.EmailInvalidException;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Tạo người dùng mới")
    public ResponseEntity<ResCreateUserDTO> createUser(@Valid @RequestBody ReqCreateUserDTO dto)
            throws EmailInvalidException {

        User user = this.userService.convertToReqCreateUserDTO(dto);

        boolean isEmailExists = this.userService.isEmailExists(user.getEmail());
        if (isEmailExists) {
            throw new EmailInvalidException("Email '" + user.getEmail() + "' đã tồn tại");
        }

        // Mã hóa mật khẩu
        String encodedPassword = this.passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);

        User userCreate = this.userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResCreateUserDTO(userCreate));
    }

    @GetMapping("/users")
    @ApiMessage("Lấy danh sách tất cả người dùng")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(spec, pageable));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Lấy thông tin người dùng theo ID")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") Long id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        return ResponseEntity.ok(this.userService.convertToResUserDTO(user));
    }

    @PutMapping("/users")
    @ApiMessage("Cập nhật thông tin người dùng")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@Valid @RequestBody ReqUpdateUserDTO dto)
            throws IdInvalidException {

        User user = this.userService.convertToReqUpdateUserDTO(dto);
        User userUpdate = this.userService.updateUser(user);
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(userUpdate));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Xóa người dùng")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {
        this.userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
