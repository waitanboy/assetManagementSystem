package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.service.EmailService;
import com.assetmanagement.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        // Any authenticated user should be able to see active users for renting, 
        // or restrict to ADMIN if renting is only done by admins.
        // Assuming regular users might need it to see who has what (or it's just for rent modal).
        // Let's keep it accessible to authenticated users (default if no PreAuthorize, or we can leave it as is since FilterChain handles auth).
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/{id}")

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        userService.updateUser(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectUser(@PathVariable Long id) {
        userService.rejectUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> sendTestEmail() {
        System.out.println("[API] sendTestEmail called");
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId);
        if (user != null && user.getEmail() != null) {
            emailService.sendWelcomeEmail(user.getEmail()); // Reuse existing template for test
            return ResponseEntity.ok("테스트 이메일이 발송 큐에 등록되었습니다: " + user.getEmail());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재 사용자를 찾을 수 없거나 이메일이 없습니다.");
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody User user) {
        userService.updateMyProfile(user);
        return ResponseEntity.ok().build();
    }
}
