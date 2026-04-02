package com.example.backend.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;
    /**
     * Có thể thêm bao nhiêu Admin tùy ý
     */

    @Value("${admin.email}")
    private String ADMIN_EMAIL;

    @Value("${admin.name}")
    private String ADMIN_NAME;

    @Value("${admin.password}")
    private String ADMIN_PASSWORD;

    // private static final String ADMIN_EMAIL = "admin@gmail.com";
    // private static final String ADMIN_NAME = "Admin";
    // private static final String ADMIN_PASSWORD = "123456";

    public DatabaseInitializer(PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        ensureBookingStatusColumnCompatible();
        ensureNotificationTypeColumnCompatible();
        ensureNotificationSoundEnabledDefaults();
        ensureNotificationSoundPresetDefaults();
        ensureBankAccountConfigColumnsCompatible();
        ensureSecuritySettingsTableAndUserPaymentPinColumn();

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

        // EQUIPMENT
        createPermissionIfNotExists("EQUIPMENT_VIEW_LIST", "Xem danh sách thiết bị");
        createPermissionIfNotExists("EQUIPMENT_VIEW_DETAIL", "Xem chi tiết thiết bị");
        createPermissionIfNotExists("EQUIPMENT_CREATE", "Tạo thiết bị");
        createPermissionIfNotExists("EQUIPMENT_UPDATE", "Cập nhật thiết bị");
        createPermissionIfNotExists("EQUIPMENT_DELETE", "Xóa thiết bị");

        // BOOKING_EQUIPMENT
        createPermissionIfNotExists("BOOKING_EQUIPMENT_VIEW", "Xem danh sách & chi tiết mượn thiết bị");
        createPermissionIfNotExists("BOOKING_EQUIPMENT_CREATE", "Tạo đơn mượn thiết bị");
        createPermissionIfNotExists("BOOKING_EQUIPMENT_UPDATE", "Cập nhật trạng thái mượn thiết bị");

        // AI keys management
        createPermissionIfNotExists("AI_VIEW_LIST", "Xem danh sách AI keys");
        createPermissionIfNotExists("AI_CREATE", "Thêm AI key");
        createPermissionIfNotExists("AI_UPDATE", "Bật/tắt AI key");
        createPermissionIfNotExists("AI_DELETE", "Xóa AI key");

        // AI chat
        createPermissionIfNotExists("AI_CHAT_ADMIN", "Admin sử dụng AI chat không giới hạn");

        // SYSTEM CONFIG - MAIL
        createPermissionIfNotExists("SYSTEM_CONFIG_MAIL_VIEW_LIST", "Xem danh sách cấu hình email gửi");
        createPermissionIfNotExists("SYSTEM_CONFIG_MAIL_CREATE", "Thêm cấu hình email gửi");
        createPermissionIfNotExists("SYSTEM_CONFIG_MAIL_UPDATE", "Cập nhật cấu hình email gửi");
        createPermissionIfNotExists("SYSTEM_CONFIG_MAIL_DELETE", "Xóa cấu hình email gửi");

        // SYSTEM CONFIG - BANK
        createPermissionIfNotExists("SYSTEM_CONFIG_BANK_VIEW_LIST", "Xem danh sách tài khoản ngân hàng");
        createPermissionIfNotExists("SYSTEM_CONFIG_BANK_CREATE", "Thêm tài khoản ngân hàng");
        createPermissionIfNotExists("SYSTEM_CONFIG_BANK_UPDATE", "Cập nhật tài khoản ngân hàng");
        createPermissionIfNotExists("SYSTEM_CONFIG_BANK_DELETE", "Xóa tài khoản ngân hàng");

        // SYSTEM CONFIG - MESSENGER
        createPermissionIfNotExists("SYSTEM_CONFIG_MESSENGER_VIEW_LIST", "Xem danh sách cấu hình messenger");
        createPermissionIfNotExists("SYSTEM_CONFIG_MESSENGER_CREATE", "Thêm cấu hình messenger");
        createPermissionIfNotExists("SYSTEM_CONFIG_MESSENGER_UPDATE", "Cập nhật cấu hình messenger");
        createPermissionIfNotExists("SYSTEM_CONFIG_MESSENGER_DELETE", "Xóa cấu hình messenger");

        // SYSTEM CONFIG - SECURITY (PIN xác nhận thanh toán)
        createPermissionIfNotExists("SYSTEM_CONFIG_SECURITY_VIEW_LIST", "Xem cấu hình bảo mật bổ sung");
        createPermissionIfNotExists("SYSTEM_CONFIG_SECURITY_UPDATE", "Cập nhật bắt buộc PIN xác nhận thanh toán");

        // SUPPORT — trang Hỗ trợ & Bảo trì admin
        createPermissionIfNotExists("SUPPORT_VIEW_LIST", "Xem nội dung trang hỗ trợ & bảo trì");
        createPermissionIfNotExists("SUPPORT_MANAGE", "Thêm/sửa/xóa liên hệ, hướng dẫn sự cố, link, ghi chú bảo trì");

        // 2. Tạo ROLES nếu chưa có
        // if (countRoles == 0) {
        // List<Permission> allPermissions = permissionRepository.findAll();

        // // ADMIN role (full quyền, gán sau user)
        // Role adminRole = new Role();
        // adminRole.setName("ADMIN");
        // adminRole.setDescription("Admin full quyền");
        // roleRepository.save(adminRole);

        // Set<Permission> viewPermissions = new HashSet<>();
        // for (Permission p : allPermissions) {

        // // PITCH: chỉ xem
        // if (p.getName().startsWith("PITCH_") &&
        // (p.getName().endsWith("_VIEW_LIST") || p.getName().endsWith("_VIEW_DETAIL")))
        // {
        // viewPermissions.add(p);
        // }

        // // BOOKING: cho C-R-U, KHÔNG cho D
        // if (p.getName().startsWith("BOOKING_")
        // && !p.getName().equals("BOOKING_DELETE")) {
        // viewPermissions.add(p);
        // }
        // }

        // Role viewRole = new Role();
        // viewRole.setName("VIEW");
        // viewRole.setDescription("Chỉ xem PITCH, tạo/sửa booking (không được xóa)");
        // viewRole.setPermissions(viewPermissions);
        // roleRepository.save(viewRole);

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

            // SUPPORT: chỉ xem (API đọc nội dung trang hỗ trợ)
            if (p.getName().equals("SUPPORT_VIEW_LIST")) {
                viewPermissions.add(p);
            }
        }

        if (viewRole == null) {
            viewRole = new Role();
            viewRole.setName("VIEW");
        }

        viewRole.setDescription("User thường - xem pitch, tạo/sửa booking");
        viewRole.setPermissions(viewPermissions);
        roleRepository.save(viewRole);

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

    private void ensureBookingStatusColumnCompatible() {
        try {
            String dataType = jdbcTemplate.queryForObject(
                    """
                            SELECT DATA_TYPE
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'bookings'
                              AND COLUMN_NAME = 'status'
                            """,
                    String.class);

            if (dataType == null || !"enum".equalsIgnoreCase(dataType)) {
                return;
            }

            // Convert old ENUM to VARCHAR so future enum values do not break inserts.
            jdbcTemplate.execute("ALTER TABLE bookings MODIFY COLUMN status VARCHAR(32) NOT NULL");
            System.out.println(">>> MIGRATION: bookings.status converted from ENUM to VARCHAR(32)");
        } catch (Exception ex) {
            // Ignore when table/column is not ready yet; app can continue bootstrap safely.
            System.out.println(">>> MIGRATION SKIPPED: " + ex.getMessage());
        }
    }

    /** Gán mặc định bật chuông cho user cũ sau khi thêm cột notification_sound_enabled. */
    private void ensureNotificationSoundEnabledDefaults() {
        try {
            jdbcTemplate.execute(
                    "UPDATE users SET notification_sound_enabled = TRUE WHERE notification_sound_enabled IS NULL");
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED notification_sound_enabled: " + ex.getMessage());
        }
    }

    /** Gán kiểu chuông DEFAULT cho user cũ sau khi thêm cột notification_sound_preset. */
    private void ensureNotificationSoundPresetDefaults() {
        try {
            jdbcTemplate.execute(
                    "UPDATE users SET notification_sound_preset = 'DEFAULT' WHERE notification_sound_preset IS NULL OR notification_sound_preset = ''");
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED notification_sound_preset: " + ex.getMessage());
        }
    }

    private void ensureNotificationTypeColumnCompatible() {
        try {
            String dataType = jdbcTemplate.queryForObject(
                    """
                            SELECT DATA_TYPE
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'notifications'
                              AND COLUMN_NAME = 'type'
                            """,
                    String.class);

            if (dataType == null || !"enum".equalsIgnoreCase(dataType)) {
                return;
            }

            // Convert old ENUM to VARCHAR so future enum values do not break inserts.
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN type VARCHAR(64) NOT NULL");
            System.out.println(">>> MIGRATION: notifications.type converted from ENUM to VARCHAR(64)");
        } catch (Exception ex) {
            // Ignore when table/column is not ready yet; app can continue bootstrap safely.
            System.out.println(">>> MIGRATION SKIPPED: " + ex.getMessage());
        }
    }

    /**
     * Chuẩn hóa bảng bank_account_configs sau khi đổi model:
     * - Bỏ cột legacy: bank_type_encrypted, bank_name_encrypted
     * - Đảm bảo có cột account_name_encrypted
     */
    /**
     * Bảng singleton security_settings + cột hash PIN trên users (không lưu PIN rõ).
     */
    private void ensureSecuritySettingsTableAndUserPaymentPinColumn() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS security_settings (
                        id BIGINT NOT NULL PRIMARY KEY,
                        payment_confirmation_pin_required BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);
            jdbcTemplate.update(
                    """
                            INSERT INTO security_settings (id, payment_confirmation_pin_required)
                            VALUES (1, FALSE)
                            ON DUPLICATE KEY UPDATE id = id
                            """);
            System.out.println(">>> MIGRATION: security_settings ensured");
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED security_settings: " + ex.getMessage());
        }

        try {
            Integer col = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'users'
                              AND COLUMN_NAME = 'payment_pin_hash'
                            """,
                    Integer.class);
            if (col != null && col == 0) {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN payment_pin_hash VARCHAR(255) NULL");
                System.out.println(">>> MIGRATION: users.payment_pin_hash added");
            }
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED payment_pin_hash: " + ex.getMessage());
        }
    }

    private void ensureBankAccountConfigColumnsCompatible() {
        try {
            Integer accountNameCol = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'bank_account_configs'
                              AND COLUMN_NAME = 'account_name_encrypted'
                            """,
                    Integer.class);
            if (accountNameCol != null && accountNameCol == 0) {
                jdbcTemplate.execute(
                        "ALTER TABLE bank_account_configs ADD COLUMN account_name_encrypted VARCHAR(1000) NULL");
                jdbcTemplate.execute(
                        "UPDATE bank_account_configs SET account_name_encrypted = '' WHERE account_name_encrypted IS NULL");
                jdbcTemplate.execute(
                        "ALTER TABLE bank_account_configs MODIFY COLUMN account_name_encrypted VARCHAR(1000) NOT NULL");
                System.out.println(">>> MIGRATION: bank_account_configs.account_name_encrypted added");
            }
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED account_name_encrypted: " + ex.getMessage());
        }

        try {
            Integer bankTypeCol = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'bank_account_configs'
                              AND COLUMN_NAME = 'bank_type_encrypted'
                            """,
                    Integer.class);
            if (bankTypeCol != null && bankTypeCol > 0) {
                jdbcTemplate.execute("ALTER TABLE bank_account_configs DROP COLUMN bank_type_encrypted");
                System.out.println(">>> MIGRATION: bank_account_configs.bank_type_encrypted dropped");
            }
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED bank_type_encrypted: " + ex.getMessage());
        }

        try {
            Integer bankNameCol = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM INFORMATION_SCHEMA.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'bank_account_configs'
                              AND COLUMN_NAME = 'bank_name_encrypted'
                            """,
                    Integer.class);
            if (bankNameCol != null && bankNameCol > 0) {
                jdbcTemplate.execute("ALTER TABLE bank_account_configs DROP COLUMN bank_name_encrypted");
                System.out.println(">>> MIGRATION: bank_account_configs.bank_name_encrypted dropped");
            }
        } catch (Exception ex) {
            System.out.println(">>> MIGRATION SKIPPED bank_name_encrypted: " + ex.getMessage());
        }
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
