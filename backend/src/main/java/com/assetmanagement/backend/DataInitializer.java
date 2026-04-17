package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(ApplicationArguments args) {
        // 모든 사용자 목록 출력 (디버깅용)
        log.info("--- 현재 등록된 사용자 목록 ---");
        userMapper.findAll().forEach(user -> 
            log.info("ID: {}, Email: {}, Role: {}, Status: {}", user.getId(), user.getEmail(), user.getRole(), user.getStatus())
        );
        log.info("----------------------------");

        // 관리자 계정 처리 (waitanboy@gmail.com -> admin@asset.com 업데이트 포함)
        String targetAdminEmail = "admin@asset.com";
        User oldAdmin = userMapper.findByEmail("admin@asset.com");
        User newAdmin = userMapper.findByEmail(targetAdminEmail);

        if (oldAdmin != null && newAdmin == null) {
            log.info("기존 기본 관리자(admin@asset.com)를 {}로 업데이트합니다.", targetAdminEmail);
            oldAdmin.setEmail(targetAdminEmail);
            userMapper.update(oldAdmin);
        } else if (oldAdmin == null && newAdmin == null) {
            User admin = User.builder()
                    .email(targetAdminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .department("IT")
                    .status("APPROVED")
                    .build();
            userMapper.insert(admin);
            log.info("✅ 새로운 관리자 계정이 생성되었습니다. (email: {} / password: admin123)", targetAdminEmail);
        }

        // 시스템 자동 알림용 계정 생성
        String systemAdminEmail = "assetSysAdmin@asset.com";
        if (userMapper.findByEmail(systemAdminEmail) == null) {
            User sysAdmin = User.builder()
                    .email(systemAdminEmail)
                    .password(passwordEncoder.encode("sysadmin123"))
                    .name("시스템 알림")
                    .role("ADMIN")
                    .department("SYSTEM")
                    .status("APPROVED")
                    .build();
            userMapper.insert(sysAdmin);
            log.info("✅ 시스템 자동 알림용 계정이 생성되었습니다. (email: {})", systemAdminEmail);
        }
    }
}
