package com.example.agentapp.auth.config;

import com.example.agentapp.auth.model.*;
import com.example.agentapp.auth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {


    private final PermissionRepository permissionRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(PermissionRepository permissionRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            System.out.println("🚀 Starting data initialization...");

            // 1. Permissions oluştur
            createPermissions();

            // 2. Roles oluştur + Permissions ata
            createRoles();

            // 3. Super Admin user oluştur
            createSuperAdmin();

            System.out.println("✅ Data initialization completed!");
        };
    }

    private void createPermissions() {
        System.out.println("📝 Creating permissions...");

        for (PermissionName permissionName : PermissionName.values()) {

            // Zaten varsa oluşturma
            if (permissionRepository.existsByName(permissionName)) {
                continue;
            }

            Permission permission = new Permission();
            permission.setName(permissionName);
            permissionRepository.save(permission);

            System.out.println("  ✓ Created permission: " + permissionName);
        }
    }

    private void createRoles() {
        System.out.println("👥 Creating roles...");

        // ============================================================
        // 1. CUSTOMER ROLE
        // ============================================================
        createRoleIfNotExists(
                "CUSTOMER",
                "Customer role with basic permissions",
                Arrays.asList(
                        // Kendi profili
                        PermissionName.USER_READ,
                        PermissionName.USER_UPDATE,

                        // Turları görüntüleme
                        PermissionName.TOUR_READ,
                        PermissionName.HOTEL_READ,
                        PermissionName.EXCURSION_READ,
                        PermissionName.TRANSFER_READ,

                        // Rezervasyon yapma
                        PermissionName.RESERVATION_CREATE,
                        PermissionName.RESERVATION_READ,
                        PermissionName.RESERVATION_UPDATE,

                        // Ödeme
                        PermissionName.PAYMENT_CREATE,
                        PermissionName.PAYMENT_READ
                )
        );

        // ============================================================
        // 2. AGENT ROLE
        // ============================================================
        createRoleIfNotExists(
                "AGENT",
                "Travel agent role",
                Arrays.asList(
                        // Müşteri işlemleri
                        PermissionName.CUSTOMER_CREATE,
                        PermissionName.CUSTOMER_READ,
                        PermissionName.CUSTOMER_UPDATE,

                        // Tur işlemleri
                        PermissionName.TOUR_READ,
                        PermissionName.HOTEL_READ,
                        PermissionName.EXCURSION_READ,
                        PermissionName.TRANSFER_READ,

                        // Rezervasyon yönetimi
                        PermissionName.RESERVATION_CREATE,
                        PermissionName.RESERVATION_READ,
                        PermissionName.RESERVATION_UPDATE,
                        PermissionName.RESERVATION_DELETE,

                        // Yolcu yönetimi
                        PermissionName.PASSENGER_CREATE,
                        PermissionName.PASSENGER_READ,
                        PermissionName.PASSENGER_UPDATE,
                        PermissionName.PASSENGER_DELETE,

                        // Ödeme
                        PermissionName.PAYMENT_CREATE,
                        PermissionName.PAYMENT_READ,

                        // Raporlar
                        PermissionName.REPORT_READ
                )
        );

        // ============================================================
        // 3. OFFICER ROLE (Memur/Çalışan)
        // ============================================================
        createRoleIfNotExists(
                "OFFICER",
                "Office staff role",
                Arrays.asList(
                        // Müşteri yönetimi
                        PermissionName.CUSTOMER_CREATE,
                        PermissionName.CUSTOMER_READ,
                        PermissionName.CUSTOMER_UPDATE,

                        // Tur yönetimi
                        PermissionName.TOUR_CREATE,
                        PermissionName.TOUR_READ,
                        PermissionName.TOUR_UPDATE,
                        PermissionName.HOTEL_CREATE,
                        PermissionName.HOTEL_READ,
                        PermissionName.HOTEL_UPDATE,
                        PermissionName.EXCURSION_CREATE,
                        PermissionName.EXCURSION_READ,
                        PermissionName.EXCURSION_UPDATE,
                        PermissionName.TRANSFER_CREATE,
                        PermissionName.TRANSFER_READ,
                        PermissionName.TRANSFER_UPDATE,

                        // Rezervasyon yönetimi
                        PermissionName.RESERVATION_CREATE,
                        PermissionName.RESERVATION_READ,
                        PermissionName.RESERVATION_UPDATE,
                        PermissionName.RESERVATION_DELETE,

                        // Yolcu yönetimi
                        PermissionName.PASSENGER_CREATE,
                        PermissionName.PASSENGER_READ,
                        PermissionName.PASSENGER_UPDATE,
                        PermissionName.PASSENGER_DELETE,

                        // Ödeme
                        PermissionName.PAYMENT_CREATE,
                        PermissionName.PAYMENT_READ,
                        PermissionName.PAYMENT_UPDATE,

                        // Raporlar
                        PermissionName.REPORT_READ,
                        PermissionName.REPORT_CREATE
                )
        );

        // ============================================================
        // 4. MANAGER ROLE
        // ============================================================
        createRoleIfNotExists(
                "MANAGER",
                "Manager role with extended permissions",
                Arrays.asList(
                        // Tüm CRUD işlemleri
                        PermissionName.USER_READ,
                        PermissionName.CUSTOMER_CREATE,
                        PermissionName.CUSTOMER_READ,
                        PermissionName.CUSTOMER_UPDATE,
                        PermissionName.CUSTOMER_DELETE,

                        // Tur yönetimi (full)
                        PermissionName.TOUR_CREATE,
                        PermissionName.TOUR_READ,
                        PermissionName.TOUR_UPDATE,
                        PermissionName.TOUR_DELETE,
                        PermissionName.HOTEL_CREATE,
                        PermissionName.HOTEL_READ,
                        PermissionName.HOTEL_UPDATE,
                        PermissionName.HOTEL_DELETE,
                        PermissionName.EXCURSION_CREATE,
                        PermissionName.EXCURSION_READ,
                        PermissionName.EXCURSION_UPDATE,
                        PermissionName.EXCURSION_DELETE,
                        PermissionName.TRANSFER_CREATE,
                        PermissionName.TRANSFER_READ,
                        PermissionName.TRANSFER_UPDATE,
                        PermissionName.TRANSFER_DELETE,

                        // Rezervasyon (full)
                        PermissionName.RESERVATION_CREATE,
                        PermissionName.RESERVATION_READ,
                        PermissionName.RESERVATION_UPDATE,
                        PermissionName.RESERVATION_DELETE,

                        // Yolcu (full)
                        PermissionName.PASSENGER_CREATE,
                        PermissionName.PASSENGER_READ,
                        PermissionName.PASSENGER_UPDATE,
                        PermissionName.PASSENGER_DELETE,

                        // Ödeme (full)
                        PermissionName.PAYMENT_CREATE,
                        PermissionName.PAYMENT_READ,
                        PermissionName.PAYMENT_UPDATE,
                        PermissionName.PAYMENT_REFUND,

                        // Raporlar (full)
                        PermissionName.REPORT_CREATE,
                        PermissionName.REPORT_READ,
                        PermissionName.REPORT_UPDATE,
                        PermissionName.REPORT_DELETE
                )
        );

        // ============================================================
        // 5. SUPER_ADMIN ROLE
        // ============================================================
        // Tüm permission'lar
        Set<PermissionName> allPermissions = new HashSet<>(
                Arrays.asList(PermissionName.values())
        );

        createRoleIfNotExists(
                "SUPER_ADMIN",
                "Super administrator with all permissions",
                allPermissions
        );
    }

    private void createRoleIfNotExists(
            String roleName,
            String description,
            Iterable<PermissionName> permissionNames) {

        // Zaten varsa oluşturma
        if (roleRepository.existsByName(roleName)) {
            System.out.println("  ⚠ Role already exists: " + roleName);
            return;
        }

        // Role oluştur
        Role role = new Role();
        role.setName(roleName);
        role.setDescription(description);

        // Permission'ları ekle
        Set<Permission> permissions = new HashSet<>();
        for (PermissionName permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException(
                            "Permission not found: " + permissionName
                    ));
            permissions.add(permission);
        }
        role.setPermissions(permissions);

        // Kaydet
        roleRepository.save(role);
        System.out.println("  ✓ Created role: " + roleName + " with " +
                permissions.size() + " permissions");
    }
    
    private void createSuperAdmin() {
        System.out.println("👤 Creating super admin user...");

        // Zaten varsa oluşturma
        if (userRepository.existsByUsername("admin")) {
            System.out.println("  ⚠ Admin user already exists");
            return;
        }

        // Super Admin rolünü al
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN.name())
                .orElseThrow(() -> new RuntimeException(
                        "SUPER_ADMIN role not found. Create roles first!"
                ));

        // Admin user oluştur
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@agentapp.com");
        admin.setPassword(passwordEncoder.encode("Admin123!")); // ⚠️ DEĞİŞTİR!
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setUserType(UserType.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setAccountNonLocked(true);

        // Rol ata
        admin.setRoles(new HashSet<>(Arrays.asList(superAdminRole)));

        // Kaydet
        userRepository.save(admin);

        System.out.println("  ✓ Super admin created:");
        System.out.println("    Username: admin");
        System.out.println("    Password: Admin123!");
        System.out.println("    ⚠️  CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION!");
    }
}