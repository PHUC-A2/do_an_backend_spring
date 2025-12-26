package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.user.ReqAssignRolesToUserDTO;
import com.example.backend.domain.request.user.ReqCreateUserDTO;
import com.example.backend.domain.request.user.ReqUpdateUserDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.user.ResCreateUserDTO;
import com.example.backend.domain.response.user.ResUpdateUserDTO;
import com.example.backend.domain.response.user.ResUserDetailDTO;
import com.example.backend.domain.response.user.ResUserListDTO;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ApiMessage("Tạo người dùng mới")
    public ResponseEntity<ResCreateUserDTO> createUser(
            @Valid @RequestBody ReqCreateUserDTO dto)
            throws EmailInvalidException {

        ResCreateUserDTO res = this.userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/users")
    @ApiMessage("Lấy danh sách người dùng")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                this.userService.getAllUsers(spec, pageable));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Lấy thông tin người dùng theo ID")
    public ResponseEntity<ResUserDetailDTO> getUserById(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        User user = this.userService.getUserById(id);
        return ResponseEntity.ok(
                this.userService.convertToResUserDetailDTO(user));
    }

    @PutMapping("/users/{id}")
    @ApiMessage("Cập nhật thông tin người dùng")
    public ResponseEntity<ResUpdateUserDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdateUserDTO dto)
            throws IdInvalidException {

        ResUpdateUserDTO res = this.userService.updateUser(id, dto);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Xóa người dùng")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        this.userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/assign-roles")
    @ApiMessage("Gán danh sách role cho user")
    public ResponseEntity<ResUserListDTO> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody ReqAssignRolesToUserDTO req)
            throws IdInvalidException {

        return ResponseEntity.ok(
                userService.assignRolesToUser(id, req.getRoleIds()));
    }

}
