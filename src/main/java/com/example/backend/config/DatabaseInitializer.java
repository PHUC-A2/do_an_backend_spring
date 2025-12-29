package com.example.backend.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.user.UserStatusEnum;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countPermissions = permissionRepository.count();
        long countRoles = roleRepository.count();
        long countUsers = userRepository.count();

        // 1. Tạo PERMISSION nếu chưa có
        if (countPermissions == 0) {
            List<Permission> permissions = new ArrayList<>();

            // USER
            permissions.add(createPermission("USER_VIEW_LIST", "Xem danh sách user"));
            permissions.add(createPermission("USER_VIEW_DETAIL", "Xem chi tiết user"));
            permissions.add(createPermission("USER_CREATE", "Tạo user"));
            permissions.add(createPermission("USER_UPDATE", "Cập nhật user"));
            permissions.add(createPermission("USER_DELETE", "Xóa user"));
            permissions.add(createPermission("USER_ASSIGN_ROLE", "Gắn role cho user"));

            // ROLE
            permissions.add(createPermission("ROLE_VIEW_LIST", "Xem danh sách role"));
            permissions.add(createPermission("ROLE_VIEW_DETAIL", "Xem chi tiết role"));
            permissions.add(createPermission("ROLE_CREATE", "Tạo role"));
            permissions.add(createPermission("ROLE_UPDATE", "Cập nhật role"));
            permissions.add(createPermission("ROLE_DELETE", "Xóa role"));
            permissions.add(createPermission("ROLE_ASSIGN_PERMISSION", "Gắn permission cho role"));

            // PERMISSION
            permissions.add(createPermission("PERMISSION_VIEW_LIST", "Xem danh sách permission"));
            permissions.add(createPermission("PERMISSION_VIEW_DETAIL", "Xem chi tiết permission"));
            permissions.add(createPermission("PERMISSION_CREATE", "Tạo permission"));
            permissions.add(createPermission("PERMISSION_UPDATE", "Cập nhật permission"));
            permissions.add(createPermission("PERMISSION_DELETE", "Xóa permission"));

            // PITCH
            permissions.add(createPermission("PITCH_VIEW_LIST", "Xem danh sách pitch"));
            permissions.add(createPermission("PITCH_VIEW_DETAIL", "Xem chi tiết pitch"));
            permissions.add(createPermission("PITCH_CREATE", "Tạo pitch"));
            permissions.add(createPermission("PITCH_UPDATE", "Cập nhật pitch"));
            permissions.add(createPermission("PITCH_DELETE", "Xóa pitch"));

            // BOOKING
            permissions.add(createPermission("BOOKING_VIEW_LIST", "Xem danh sách booking"));
            permissions.add(createPermission("BOOKING_VIEW_DETAIL", "Xem chi tiết booking"));
            permissions.add(createPermission("BOOKING_CREATE", "Tạo booking"));
            permissions.add(createPermission("BOOKING_UPDATE", "Cập nhật booking"));
            permissions.add(createPermission("BOOKING_DELETE", "Xóa booking"));

            permissionRepository.saveAll(permissions);
        }

        // 2. Tạo ROLES nếu chưa có
        if (countRoles == 0) {
            List<Permission> allPermissions = permissionRepository.findAll();

            // ADMIN role (full quyền, gán sau user)
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Admin full quyền");
            roleRepository.save(adminRole);

            // VIEW role: gắn VIEW_LIST & VIEW_DETAIL của PITCH + full CRUD BOOKING
            Set<Permission> viewPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getName().startsWith("PITCH_") &&
                        (p.getName().endsWith("_VIEW_LIST") || p.getName().endsWith("_VIEW_DETAIL"))) {
                    viewPermissions.add(p);
                }
                if (p.getName().startsWith("BOOKING_")) {
                    viewPermissions.add(p); // full CRUD
                }
            }
            Role viewRole = new Role();
            viewRole.setName("VIEW");
            viewRole.setDescription("Chỉ xem PITCH, full BOOKING");
            viewRole.setPermissions(viewPermissions);
            roleRepository.save(viewRole);
        }

        // 3. Tạo USER admin nếu chưa có
        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setName("Admin");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setStatus(UserStatusEnum.ACTIVE);

            Role adminRole = roleRepository.findByName("ADMIN");
            if (adminRole != null) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                adminUser.setRoles(roles);
            }

            userRepository.save(adminUser);
        }

        System.out.println(">>> END INIT DATABASE");
    }

    private Permission createPermission(String name, String description) {
        Permission p = new Permission();
        p.setName(name);
        p.setDescription(description);
        return p;
    }
}
