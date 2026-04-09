package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // 관리자 계정이 없으면 자동 생성
        User existingAdmin = userMapper.findByEmail("admin@asset.com");
        if (existingAdmin == null) {
            User admin = User.builder()
                    .email("admin@asset.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .department("IT")
                    .build();
            userMapper.insert(admin);
            log.info("✅ 관리자 계정이 생성되었습니다. (email: admin@asset.com / password: admin123)");
        } else {
            log.info("✅ 관리자 계정이 이미 존재합니다. (email: {})", existingAdmin.getEmail());
        }
    }
}
