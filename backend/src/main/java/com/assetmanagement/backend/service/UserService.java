package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.TransactionMapper;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final TransactionMapper transactionMapper;
    private final PasswordEncoder passwordEncoder;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String email = authentication.getPrincipal().toString();
        User user = userMapper.findByEmail(email);
        return user != null ? user.getId() : null;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userMapper.findApproved();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        
        logActivity(null, "CREATE", "New user registered: " + user.getEmail());
        return user;
    }

    @Transactional
    public void updateUser(Long id, User user) {
        User oldUser = userMapper.findById(id);
        user.setId(id);
        boolean passwordChanged = false;
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            passwordChanged = true;
        }
        userMapper.update(user);

        String diffNote = buildUserDiff(oldUser, user, passwordChanged);
        logActivity(null, "UPDATE", "User updated: " + user.getEmail() + diffNote);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            userMapper.delete(id);
            logActivity(null, "DELETE", "User deleted: " + user.getEmail());
        }
    }

    @Transactional
    public void approveUser(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setStatus("APPROVED");
            userMapper.update(user);
            logActivity(null, "UPDATE", "User approved: " + user.getEmail());
        }
    }

    @Transactional
    public void rejectUser(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setStatus("REJECTED");
            userMapper.update(user);
            logActivity(null, "UPDATE", "User rejected: " + user.getEmail());
        }
    }

    private void logActivity(Long assetId, String type, String note) {
        Long actorId = getCurrentUserId();
        if (actorId == null) return; // Should ideally not happen if authenticated

        Transaction tx = Transaction.builder()
                .assetId(assetId)
                .userId(actorId)
                .type(type)
                .note(note)
                .build();
        transactionMapper.insert(tx);
    }

    private String buildUserDiff(User oldUser, User newUser, boolean passwordChanged) {
        if (oldUser == null) return "";
        List<String> changes = new ArrayList<>();

        if (!Objects.equals(oldUser.getEmail(), newUser.getEmail())) {
            changes.add("이메일: '" + oldUser.getEmail() + "' → '" + newUser.getEmail() + "'");
        }
        if (!Objects.equals(oldUser.getRole(), newUser.getRole())) {
            changes.add("역할: '" + oldUser.getRole() + "' → '" + newUser.getRole() + "'");
        }
        if (!Objects.equals(oldUser.getDepartment(), newUser.getDepartment())) {
            changes.add("부서: '" + oldUser.getDepartment() + "' → '" + newUser.getDepartment() + "'");
        }
        if (passwordChanged) {
            changes.add("비밀번호 변경됨");
        }

        return changes.isEmpty() ? " (변경사항 없음)" : " [변경내역: " + String.join(", ", changes) + "]";
    }
}
