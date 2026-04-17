package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-email}")
    private String adminEmail;

    @Value("${app.init.admin-password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        // Ensure initial admin account exists
        if (userMapper.findByEmail(adminEmail) == null) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("Administrator")
                    .role("ADMIN")
                    .department("IT")
                    .status("APPROVED")
                    .build();
            userMapper.insert(admin);
            log.info("✅ Initial admin account created: {}", adminEmail);
        }

        // Ensure system notification account exists
        String systemAdminEmail = "assetSysAdmin@asset.com";
        if (userMapper.findByEmail(systemAdminEmail) == null) {
            User sysAdmin = User.builder()
                    .email(systemAdminEmail)
                    .password(passwordEncoder.encode("sysadmin123"))
                    .name("System Notification")
                    .role("ADMIN")
                    .department("SYSTEM")
                    .status("APPROVED")
                    .build();
            userMapper.insert(sysAdmin);
            log.info("✅ System notification account created: {}", systemAdminEmail);
        }
    }
}
