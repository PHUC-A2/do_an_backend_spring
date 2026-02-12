package com.example.backend.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Permission;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.user.UserStatusEnum;

@Service
@Transactional
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_PASSWORD = "123456";

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

        // long countPermissions = permissionRepository.count();
        // 1. Tạo PERMISSION nếu chưa có

        // ================== INIT PERMISSIONS ==================

        // USER
        createPermissionIfNotExists("USER_VIEW_LIST", "Xem danh sách user");
        createPermissionIfNotExists("USER_VIEW_DETAIL", "Xem chi tiết user");
        createPermissionIfNotExists("USER_CREATE", "Tạo user");
        createPermissionIfNotExists("USER_UPDATE", "Cập nhật user");
        createPermissionIfNotExists("USER_DELETE", "Xóa user");
        createPermissionIfNotExists("USER_ASSIGN_ROLE", "Gắn role cho user");

        // ROLE
        createPermissionIfNotExists("ROLE_VIEW_LIST", "Xem danh sách role");
        createPermissionIfNotExists("ROLE_VIEW_DETAIL", "Xem chi tiết role");
        createPermissionIfNotExists("ROLE_CREATE", "Tạo role");
        createPermissionIfNotExists("ROLE_UPDATE", "Cập nhật role");
        createPermissionIfNotExists("ROLE_DELETE", "Xóa role");
        createPermissionIfNotExists("ROLE_ASSIGN_PERMISSION", "Gắn permission cho role");

        // PERMISSION
        createPermissionIfNotExists("PERMISSION_VIEW_LIST", "Xem danh sách permission");
        createPermissionIfNotExists("PERMISSION_VIEW_DETAIL", "Xem chi tiết permission");
        createPermissionIfNotExists("PERMISSION_CREATE", "Tạo permission");
        createPermissionIfNotExists("PERMISSION_UPDATE", "Cập nhật permission");
        createPermissionIfNotExists("PERMISSION_DELETE", "Xóa permission");

        // PITCH
        createPermissionIfNotExists("PITCH_VIEW_LIST", "Xem danh sách pitch");
        createPermissionIfNotExists("PITCH_VIEW_DETAIL", "Xem chi tiết pitch");
        createPermissionIfNotExists("PITCH_CREATE", "Tạo pitch");
        createPermissionIfNotExists("PITCH_UPDATE", "Cập nhật pitch");
        createPermissionIfNotExists("PITCH_DELETE", "Xóa pitch");

        // BOOKING
        createPermissionIfNotExists("BOOKING_VIEW_LIST", "Xem danh sách booking");
        createPermissionIfNotExists("BOOKING_VIEW_DETAIL", "Xem chi tiết booking");
        createPermissionIfNotExists("BOOKING_CREATE", "Tạo booking");
        createPermissionIfNotExists("BOOKING_UPDATE", "Cập nhật booking");
        createPermissionIfNotExists("BOOKING_DELETE", "Xóa booking");

        // PAYMENT
        createPermissionIfNotExists("PAYMENT_VIEW_LIST", "Danh sách payment chờ xác nhận");
        createPermissionIfNotExists("PAYMENT_UPDATE", "Admin xác nhận payment đã thanh toán");

        // REVENUE
        createPermissionIfNotExists("REVENUE_VIEW_DETAIL", "Lấy thống kê doanh thu");

        // 2. Tạo ROLES nếu chưa có
        // if (countRoles == 0) {
        //     List<Permission> allPermissions = permissionRepository.findAll();

        //     // ADMIN role (full quyền, gán sau user)
        //     Role adminRole = new Role();
        //     adminRole.setName("ADMIN");
        //     adminRole.setDescription("Admin full quyền");
        //     roleRepository.save(adminRole);

        //     Set<Permission> viewPermissions = new HashSet<>();
        //     for (Permission p : allPermissions) {

        //         // PITCH: chỉ xem
        //         if (p.getName().startsWith("PITCH_") &&
        //                 (p.getName().endsWith("_VIEW_LIST") || p.getName().endsWith("_VIEW_DETAIL"))) {
        //             viewPermissions.add(p);
        //         }

        //         // BOOKING: cho C-R-U, KHÔNG cho D
        //         if (p.getName().startsWith("BOOKING_")
        //                 && !p.getName().equals("BOOKING_DELETE")) {
        //             viewPermissions.add(p);
        //         }
        //     }

        //     Role viewRole = new Role();
        //     viewRole.setName("VIEW");
        //     viewRole.setDescription("Chỉ xem PITCH, tạo/sửa booking (không được xóa)");
        //     viewRole.setPermissions(viewPermissions);
        //     roleRepository.save(viewRole);

        // }

        // ================== INIT ROLE ADMIN ==================
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Admin full quyền (không cần gắn permission)");
            roleRepository.save(adminRole);
        }

        // ================== INIT ROLE VIEW ==================
        Role viewRole = roleRepository.findByName("VIEW");
        if (viewRole == null) {

            List<Permission> allPermissions = permissionRepository.findAll();
            Set<Permission> viewPermissions = new HashSet<>();

            for (Permission p : allPermissions) {

                // PITCH: chỉ xem
                if (p.getName().startsWith("PITCH_") &&
                        (p.getName().endsWith("_VIEW_LIST") || p.getName().endsWith("_VIEW_DETAIL"))) {
                    viewPermissions.add(p);
                }

                // BOOKING: cho C-R-U, KHÔNG cho DELETE
                if (p.getName().startsWith("BOOKING_")
                        && !p.getName().equals("BOOKING_DELETE")) {
                    viewPermissions.add(p);
                }
            }

            viewRole = new Role();
            viewRole.setName("VIEW");
            viewRole.setDescription("User thường - xem pitch, tạo/sửa booking");
            viewRole.setPermissions(viewPermissions);

            roleRepository.save(viewRole);
        }

        // 3. Tạo USER admin nếu chưa có
        User existingAdmin = userRepository.findByEmail(ADMIN_EMAIL);

        if (existingAdmin == null) {
            User adminUser = new User();
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setName(ADMIN_NAME);
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            adminUser.setStatus(UserStatusEnum.ACTIVE);

            if (adminRole != null) {
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                adminUser.setRoles(roles);
            }

            userRepository.save(adminUser);
        }

        // 4. Đảm bảo admin luôn có role ADMIN (FIX BUG)
        if (adminRole != null) {
            User adminUser = userRepository.findByEmail(ADMIN_EMAIL);

            if (adminUser != null) {
                if (adminUser.getRoles() == null) {
                    adminUser.setRoles(new HashSet<>());
                }

                if (!adminUser.getRoles().contains(adminRole)) {
                    adminUser.getRoles().add(adminRole);
                    userRepository.save(adminUser);
                }
            }
        }

        System.out.println(">>> END INIT DATABASE");
    }

    // private Permission createPermission(String name, String description) {
    // Permission p = new Permission();
    // p.setName(name);
    // p.setDescription(description);
    // return p;
    // }

    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
        }
    }

}
