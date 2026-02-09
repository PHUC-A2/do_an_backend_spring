package com.example.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.account.ReqUpdateAccountDTO;
import com.example.backend.domain.request.auth.ReqRegisterDTO;
import com.example.backend.domain.request.user.ReqCreateUserDTO;
import com.example.backend.domain.request.user.ReqUpdateUserDTO;
import com.example.backend.domain.response.account.AccountUserUpdateDTO;
import com.example.backend.domain.response.account.ResUpdateAccountDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.permission.ResPermissionNestedDTO;
import com.example.backend.domain.response.role.ResRoleNestedDTO;
import com.example.backend.domain.response.role.ResRoleNestedDetailDTO;
import com.example.backend.domain.response.user.ResCreateUserDTO;
import com.example.backend.domain.response.user.ResUpdateUserDTO;
import com.example.backend.domain.response.user.ResUserDetailDTO;
import com.example.backend.domain.response.user.ResUserListDTO;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.error.EmailInvalidException;
import com.example.backend.util.error.IdInvalidException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public ResCreateUserDTO createUser(ReqCreateUserDTO req)
            throws EmailInvalidException {

        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new EmailInvalidException("Email '" + req.getEmail() + "' đã tồn tại");
        }

        User user = this.convertToReqCreateUser(req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // Lấy role VIEW và gắn mặc định
        Role viewRole = this.roleRepository.findByName("VIEW");
        if (viewRole != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(viewRole);
            user.setRoles(roles);
        }

        User savedUser = this.userRepository.save(user);
        return this.convertToResCreateUserDTO(savedUser);
    }

    public ResultPaginationDTO getAllUsers(@Nullable Specification<User> spec, @NonNull Pageable pageable) {

        Page<User> pageUser = this.userRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        rs.setMeta(meta);

        List<ResUserListDTO> resList = new ArrayList<>();
        for (User user : pageUser.getContent()) {
            resList.add(this.convertToResUserListDTO(user));
        }

        rs.setResult(resList);
        return rs;
    }

    // public User getUserById(Long id) throws IdInvalidException {

    // Optional<User> optionalUser = this.userRepository.findById(id);
    // if (optionalUser.isPresent()) {
    // return optionalUser.get();
    // }
    // throw new IdInvalidException("Không tìm thấy User với ID = " + id);
    // }

    public User getUserById(Long id) throws IdInvalidException {
        return userRepository.findWithRolesAndPermissionsById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy User với ID = " + id));
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

    public void deleteUser(@NonNull Long id) throws IdInvalidException {
        // User user = this.getUserById(id);
        this.getUserById(id);
        this.userRepository.deleteById(id);
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
    public ResUserListDTO convertToResUserListDTO(User user) {

        ResUserListDTO res = new ResUserListDTO();
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

        // map roles + permissions
        res.setRoles(
                user.getRoles().stream()
                        .map(role -> {
                            ResRoleNestedDTO roleDTO = new ResRoleNestedDTO();
                            roleDTO.setId(role.getId());
                            roleDTO.setName(role.getName());
                            roleDTO.setDescription(role.getDescription());

                            return roleDTO;
                        })
                        .toList());

        return res;
    }

    // entity -> res get
    public ResUserDetailDTO convertToResUserDetailDTO(User user) {

        ResUserDetailDTO res = new ResUserDetailDTO();
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

        // map roles + permissions
        res.setRoles(
                user.getRoles().stream()
                        .map(role -> {
                            ResRoleNestedDetailDTO roleDTO = new ResRoleNestedDetailDTO();
                            roleDTO.setId(role.getId());
                            roleDTO.setName(role.getName());
                            roleDTO.setDescription(role.getDescription());

                            roleDTO.setPermissions(
                                    role.getPermissions().stream()
                                            .map(p -> {
                                                ResPermissionNestedDTO pDTO = new ResPermissionNestedDTO();
                                                pDTO.setId(p.getId());
                                                pDTO.setName(p.getName());
                                                pDTO.setDescription(p.getDescription());
                                                return pDTO;
                                            })
                                            .toList());

                            return roleDTO;
                        })
                        .toList());

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
    public User createUserForRegister(@NonNull User user) {
        return this.userRepository.save(user);
    }

    // gắn role cho user
    public ResUserListDTO assignRolesToUser(
            @NonNull Long userId,
            List<Long> roleIds) throws IdInvalidException {

        // 1. Check null
        // if (roleIds.equals(null)) {
        // throw new IdInvalidException("roleIds không được null");
        // }

        // 2. Check user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy user với id = " + userId));

        // 3. Clear roles
        if (roleIds.isEmpty()) {
            user.getRoles().clear();
            userRepository.save(user);
            return convertToResUserListDTO(user);
        }

        // 4. Check role tồn tại
        List<Role> roles = this.roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new IdInvalidException("Có role không tồn tại");
        }

        // 5. Sync (giống Laravel sync)
        user.getRoles().clear();
        user.getRoles().addAll(roles);

        User savedUser = userRepository.save(user);
        return this.convertToResUserListDTO(savedUser);
    }

    // res update account
    public ResUpdateAccountDTO convertToResUpdateAccountDTO(User user) {

        AccountUserUpdateDTO dto = new AccountUserUpdateDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAvatarUrl(user.getAvatarUrl());

        ResUpdateAccountDTO res = new ResUpdateAccountDTO();
        res.setUser(dto);

        return res;
    }

    // update account
    public ResUpdateAccountDTO updateAccount(ReqUpdateAccountDTO req) {

        // Lấy user từ token
        // user từ token (client đặt)
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        User user = this.handleGetUserByUsername(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (req.getName() != null) {
            user.setName(StringUtils.hasText(req.getName()) ? req.getName() : null);
        }

        if (req.getFullName() != null) {
            user.setFullName(StringUtils.hasText(req.getFullName()) ? req.getFullName() : null);
        }

        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(StringUtils.hasText(req.getPhoneNumber()) ? req.getPhoneNumber() : null);
        }

        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(StringUtils.hasText(req.getAvatarUrl()) ? req.getAvatarUrl() : null);
        }

        User userSave = this.userRepository.save(user);
        return this.convertToResUpdateAccountDTO(userSave);
    }

}
