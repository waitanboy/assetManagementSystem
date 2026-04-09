package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.NotificationDTO;
import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.Notice;
import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.NoticeMapper;
import com.assetmanagement.backend.mapper.TransactionMapper;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserMapper userMapper;
    private final AssetMapper assetMapper;
    private final TransactionMapper transactionMapper;
    private final NoticeMapper noticeMapper;

    public List<NotificationDTO> getNotificationsForCurrentUser() {
        List<NotificationDTO> notifications = new ArrayList<>();
        User currentUser = getCurrentUser();
        
        if (currentUser == null) return notifications;

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        // 1. Admin Logic
        if (isAdmin) {
            // Pending Users
            long pendingCount = userMapper.findAll().stream()
                    .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                    .count();
            
            if (pendingCount > 0) {
                notifications.add(NotificationDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .type("PENDING_USER")
                        .title("Pending Approvals")
                        .message(pendingCount + " user(s) waiting for admin approval.")
                        .link("/users")
                        .timestamp(LocalDateTime.now().toString())
                        .isRead(false)
                        .build());
            }

            // Overdue Assets (dueDate < today)
            List<Asset> rentedAssets = assetMapper.findAll(null, "RENTED");
            int overdueCount = 0;
            LocalDate today = LocalDate.now();

            for (Asset asset : rentedAssets) {
                List<Transaction> txs = transactionMapper.findByAssetId(asset.getId());
                if (!txs.isEmpty()) {
                    Transaction lastTx = txs.get(0); // Assuming ordered by DESC
                    if ("RENT".equalsIgnoreCase(lastTx.getType()) && lastTx.getDueDate() != null) {
                        if (lastTx.getDueDate().isBefore(today)) {
                            overdueCount++;
                        }
                    }
                }
            }

            if (overdueCount > 0) {
                notifications.add(NotificationDTO.builder()
                        .id(UUID.randomUUID().toString())
                        .type("OVERDUE_ASSET")
                        .title("Overdue Assets")
                        .message(overdueCount + " asset(s) have passed their return due date.")
                        .link("/")
                        .timestamp(LocalDateTime.now().toString())
                        .isRead(false)
                        .build());
            }
        }

        // 2. User Logic
        // My Active Rented Assets & Overdue Check
        List<Asset> allRented = assetMapper.findAll(null, "RENTED");
        List<Asset> myRentedAssets = new ArrayList<>();
        int myOverdueCount = 0;
        LocalDate today = LocalDate.now();
        
        for (Asset asset : allRented) {
            List<Transaction> txs = transactionMapper.findByAssetId(asset.getId());
            if (!txs.isEmpty()) {
                Transaction lastTx = txs.get(0);
                if ("RENT".equalsIgnoreCase(lastTx.getType()) && currentUser.getEmail().equals(lastTx.getUserEmail())) {
                    myRentedAssets.add(asset);
                    if (lastTx.getDueDate() != null && lastTx.getDueDate().isBefore(today)) {
                        myOverdueCount++;
                    }
                }
            }
        }

        if (myOverdueCount > 0) {
            notifications.add(NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .type("USER_OVERDUE_ASSET")
                    .title("⚠️ Return Overdue")
                    .message("You have " + myOverdueCount + " asset(s) that have passed their return due date. Please return them immediately.")
                    .link("/my-assets")
                    .timestamp(LocalDateTime.now().toString())
                    .isRead(false)
                    .build());
        }

        if (!myRentedAssets.isEmpty()) {
            notifications.add(NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .type("MY_RENTED_ASSET")
                    .title("Rented Assets")
                    .message("You currently have " + myRentedAssets.size() + " asset(s) in possession.")
                    .link("/my-assets") 
                    .timestamp(LocalDateTime.now().toString())
                    .isRead(false)
                    .build());
        }

        // 3. New Notices (Last 7 Days)
        List<Notice> recentNotices = noticeMapper.findAll().stream()
                .filter(n -> ChronoUnit.DAYS.between(n.getCreatedAt(), LocalDateTime.now()) <= 7)
                .collect(Collectors.toList());

        if (!recentNotices.isEmpty()) {
            notifications.add(NotificationDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .type("NEW_NOTICE")
                    .title("New Announcements")
                    .message("There are " + recentNotices.size() + " new announcements.")
                    .link("/notices")
                    .timestamp(LocalDateTime.now().toString())
                    .isRead(false)
                    .build());
        }

        return notifications;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userMapper.findByEmail(auth.getPrincipal().toString());
        }
        return null;
    }
}
