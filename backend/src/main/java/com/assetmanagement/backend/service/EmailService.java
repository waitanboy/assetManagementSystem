package com.assetmanagement.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error while sending email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String toEmail) {
        String subject = "[Asset Management] 가입 승인 안내";
        String content = "<h1>환영합니다!</h1>" +
                "<p>귀하의 계정이 관리자에 의해 승인되었습니다.</p>" +
                "<p>이제 시스템에 로그인하여 자산을 관리하실 수 있습니다.</p>" +
                "<br><p>사내 자산 관리 시스템 팀 드림</p>";
        sendEmail(toEmail, subject, content);
    }

    public void sendRentalConfirmation(String toEmail, String assetName, String dueDate) {
        String subject = "[Asset Management] 자산 대여 확인 안내";
        String content = "<h2>자산 대여가 완료되었습니다.</h2>" +
                "<p><b>대여 자산:</b> " + assetName + "</p>" +
                "<p><b>반납 예정일:</b> <span style='color: #e11d48; font-weight: bold;'>" + dueDate + "</span></p>" +
                "<p>기한 내에 안전하게 사용 후 반납 부탁드립니다.</p>" +
                "<br><p>사내 자산 관리 시스템 팀 드림</p>";
        sendEmail(toEmail, subject, content);
    }

    public void sendOverdueAlert(String toEmail, String assetName, String dueDate) {
        String subject = "🚨 [경고] 자산 반납 기한 연체 안내";
        String content = "<div style='border: 2px solid #ef4444; padding: 20px; border-radius: 10px;'>" +
                "<h2 style='color: #ef4444;'>자산 반납 기한이 경과되었습니다!</h2>" +
                "<p>안녕하세요. 현재 다음 자산의 반납 기한이 지났습니다.</p>" +
                "<p><b>연체 자산:</b> " + assetName + "</p>" +
                "<p><b>원래 기한:</b> " + dueDate + "</p>" +
                "<p style='font-weight: bold; margin-top: 15px;'>다른 직원을 위해 즉시 반납해 주시거나, 관리팀에 연락해 기한 연장을 요청해 주세요.</p>" +
                "</div>" +
                "<br><p>사내 자산 관리 시스템 팀 드림</p>";
        sendEmail(toEmail, subject, content);
    }
}
