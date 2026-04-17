package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.ChatMessage;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.AssetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverdueNotificationService {

    private final AssetMapper assetMapper;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 매일 오전 10시에 연체된 자산을 확인하여 채팅으로 알림을 발송합니다.
     * (cron = "0 0 10 * * *")
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void checkAndNotifyOverdueAssets() {
        log.info("[OverdueCheck] Starting daily overdue asset check...");
        
        List<Asset> overdueAssets = assetMapper.findOverdueAssets();
        if (overdueAssets.isEmpty()) {
            log.info("[OverdueCheck] No overdue assets found today.");
            return;
        }

        User admin = chatService.getAdminUser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Asset asset : overdueAssets) {
            String dueDateStr = asset.getDueDate() != null ? asset.getDueDate().format(formatter) : "미지정";
            String content = String.format(
                "[연체 알림] 안녕하세요, 관리자입니다. 귀하가 현재 대여 중인 자산 '%s' (시리얼: %s)의 반납 예정일(%s)이 지났습니다. 빠른 시일 내에 반납해 주시기 바랍니다.",
                asset.getName(), asset.getSerialNumber(), dueDateStr
            );

            log.info("[OverdueCheck] Sending notification to user {} for asset {}", asset.getRentedByEmail(), asset.getName());

            if (asset.getRentedByUserId() != null) {
                // 1. DB에 메시지 저장 (관리자 -> 사용자)
                ChatMessage saved = chatService.saveMessage(admin.getId(), asset.getRentedByUserId(), content);

                // 2. 실시간 WebSocket 전송 (사용자에게)
                if (asset.getRentedByEmail() != null) {
                    messagingTemplate.convertAndSendToUser(
                        asset.getRentedByEmail(),
                        "/queue/messages",
                        saved
                    );
                }

                // 3. 실시간 WebSocket 전송 (관리자에게도 전송하여 관리자 화면 실시간 업데이트)
                if (admin.getEmail() != null) {
                    messagingTemplate.convertAndSendToUser(
                        admin.getEmail(),
                        "/queue/messages",
                        saved
                    );
                }
            }
        }
        
        log.info("[OverdueCheck] Finished sending notifications for {} assets.", overdueAssets.size());
    }
}
