package com.assetmanagement.backend.scheduler;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.TransactionMapper;
import com.assetmanagement.backend.service.ChatService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final AssetMapper assetMapper;
    private final TransactionMapper transactionMapper;
    private final ChatService chatService;


    /**
     * 메일 기능 비활성화를 위해 임시 주석 처리 (Re-enable by uncommenting @Scheduled)
     * @Scheduled(cron = "0 0 9 * * *")
     */
    public void checkOverdueAndNotify() {
        System.out.println("Running scheduled overdue check...");
        
        List<Asset> rentedAssets = assetMapper.findAll(null, "RENTED", null);
        LocalDate today = LocalDate.now();
        int count = 0;

        for (Asset asset : rentedAssets) {
            List<Transaction> txs = transactionMapper.findByAssetId(asset.getId());
            if (!txs.isEmpty()) {
                Transaction lastTx = txs.get(0); // Assuming ordered by DESC
                if ("RENT".equalsIgnoreCase(lastTx.getType()) && lastTx.getDueDate() != null) {
                    if (lastTx.getDueDate().isBefore(today)) {
                        Long userId = lastTx.getUserId();
                        if (userId != null) {
                            String message = String.format("🚨 [경고] 자산 반납 기한 연체 안내\n현재 '%s' 자산의 반납 기한(%s)이 지났습니다. 즉시 반납하시거나 관리팀에 문의해 주세요.", 
                                asset.getName(), lastTx.getDueDate().toString());
                            chatService.sendSystemMessage(userId, message);
                            count++;
                        }
                    }
                }
            }
        }
        
        System.out.println("Scheduled overdue check completed. Overdue assets found: " + count);
    }
    
    /**
     * 개발용 테스트 스케줄러 (주석 해제 시 5분마다 실행)
     */
    /*
    @Scheduled(fixedRate = 300000)
    public void testOverdueCheck() {
        checkOverdueAndNotify();
    }
    */
}
