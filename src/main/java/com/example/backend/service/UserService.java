package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.auth.ReqRegisterDTO;
import com.example.backend.domain.request.user.ReqCreateUserDTO;
import com.example.backend.domain.request.user.ReqUpdateUserDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.user.ResCreateUserDTO;
import com.example.backend.domain.response.user.ResUpdateUserDTO;
import com.example.backend.domain.response.user.ResUserDTO;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.error.EmailInvalidException;
import com.example.backend.util.error.IdInvalidException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResCreateUserDTO createUser(ReqCreateUserDTO req)
            throws EmailInvalidException {

        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new EmailInvalidException("Email '" + req.getEmail() + "' đã tồn tại");
        }

        User user = this.convertToReqCreateUser(req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        User savedUser = this.userRepository.save(user);
        return this.convertToResCreateUserDTO(savedUser);
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {

        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        rs.setMeta(meta);

        List<ResUserDTO> resList = new ArrayList<>();
        for (User user : pageUser.getContent()) {
            resList.add(this.convertToResUserDTO(user));
        }

        rs.setResult(resList);
        return rs;
    }

    public User getUserById(Long id) throws IdInvalidException {

        Optional<User> optionalUser = this.userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new IdInvalidException("Không tìm thấy User với ID = " + id);
    }

    public ResUpdateUserDTO updateUser(Long id, ReqUpdateUserDTO req)
            throws IdInvalidException {

        User user = this.getUserById(id);

        user.setName(req.getName());
        user.setFullName(req.getFullName());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setStatus(req.getStatus());

        User updatedUser = this.userRepository.save(user);
        return this.convertToResUpdateUserDTO(updatedUser);
    }

    public void deleteUser(Long id) throws IdInvalidException {
        User user = this.getUserById(id);
        this.userRepository.deleteById(user.getId());
    }

    // req create -> entity
    public User convertToReqCreateUser(ReqCreateUserDTO req) {

        User user = new User();
        user.setName(req.getName());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAvatarUrl(req.getAvatarUrl());
        // user.setStatus(req.getStatus());

        return user;
    }

    // entity -> res create
    public ResCreateUserDTO convertToResCreateUserDTO(User user) {

        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());

        return res;
    }

    // entity -> res update
    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {

        ResUpdateUserDTO res = new ResUpdateUserDTO();
        res.setName(user.getName());
        res.setFullName(user.getFullName());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setStatus(user.getStatus());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }

    // entity -> res get
    public ResUserDTO convertToResUserDTO(User user) {

        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setUpdatedBy(user.getUpdatedBy());

        return res;
    }

    public User handleGetUserByUsername(String email) {
        return this.userRepository.findByEmail(email);
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    // register
    public User convertToReqRegisterDTO(ReqRegisterDTO userReq) {
        User user = new User();
        user.setName(userReq.getName());
        user.setPassword(userReq.getPassword());
        user.setEmail(userReq.getEmail());
        return user;
    }

    public boolean isEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    // Dành cho AuthController.register
    public User createUserForRegister(User user) {
        return this.userRepository.save(user);
    }

}
