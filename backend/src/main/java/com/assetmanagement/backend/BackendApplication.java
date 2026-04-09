package com.assetmanagement.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * DataInitializer 제거됨
     * 관리자 계정은 수동으로 생성하거나 별도 스크립트를 사용하세요.
     */
}
