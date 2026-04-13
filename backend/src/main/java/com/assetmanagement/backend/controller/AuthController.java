package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.LoginRequest;
import com.assetmanagement.backend.dto.SignupRequest;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import com.assetmanagement.backend.service.UserService;
import com.assetmanagement.backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        User existingUser = userMapper.findByEmail(signupRequest.getEmail());
        if (existingUser != null) {
            if ("REJECTED".equals(existingUser.getStatus())) {
                existingUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
                existingUser.setDepartment(signupRequest.getDepartment());
                existingUser.setStatus("PENDING");
                userMapper.update(existingUser);
                return ResponseEntity.status(201).body("Re-application successful! Pending admin approval.");
            }
            return ResponseEntity.status(400).body("Email is already registered");
        }

        User newUser = User.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .name(signupRequest.getName())
                .department(signupRequest.getDepartment())
                .role("USER")
                .status("PENDING")
                .build();

        userService.createUser(newUser);
        
        return ResponseEntity.status(201).body("Signup successful! Pending admin approval.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        User user = userMapper.findByEmail(loginRequest.getEmail());
        
        if (user == null) {
            System.out.println("[LOGIN] User not found with email: " + loginRequest.getEmail());
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            if ("PENDING".equals(user.getStatus())) {
                System.out.println("[LOGIN] Account pending: " + loginRequest.getEmail());
                return ResponseEntity.status(403).body("Account pending admin approval");
            }
            if ("REJECTED".equals(user.getStatus())) {
                System.out.println("[LOGIN] Account rejected: " + loginRequest.getEmail());
                return ResponseEntity.status(403).body("Account rejected by admin");
            }

            System.out.println("[LOGIN] Success: " + loginRequest.getEmail());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            
            // Set HttpOnly Cookie
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 1 day
            response.addCookie(cookie);

            Map<String, Object> body = new HashMap<>();
            body.put("id", user.getId());
            body.put("email", user.getEmail());
            body.put("name", user.getName());
            body.put("role", user.getRole());
            return ResponseEntity.ok(body);
        }
        
        System.out.println("[LOGIN] Password mismatch for: " + loginRequest.getEmail());
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getPrincipal().toString();
            User user = userMapper.findByEmail(email);
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("email", user.getEmail());
                userData.put("name", user.getName());
                userData.put("role", user.getRole());
                userData.put("department", user.getDepartment());
                userData.put("status", user.getStatus());
                return ResponseEntity.ok(userData);
            }
        }
        return ResponseEntity.status(401).build();
    }
}
