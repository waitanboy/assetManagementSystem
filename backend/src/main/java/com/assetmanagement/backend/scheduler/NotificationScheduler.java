package com.assetmanagement.backend.scheduler;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.TransactionMapper;
import com.assetmanagement.backend.service.EmailService;
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
    private final EmailService emailService;

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
                        String userEmail = lastTx.getUserEmail();
                        if (userEmail != null) {
                            emailService.sendOverdueAlert(
                                    userEmail, 
                                    asset.getName(), 
                                    lastTx.getDueDate().toString()
                            );
                            count++;
                        }
                    }
                }
            }
        }
        
        System.out.println("Scheduled overdue check completed. Emails sent: " + count);
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
