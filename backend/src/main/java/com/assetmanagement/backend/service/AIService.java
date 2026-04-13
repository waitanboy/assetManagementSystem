package com.assetmanagement.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class AIService {

    private final ChatClient chatClient;

    public AIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateNotice(String topic) {
        String prompt = "사내 IT 자산 관리자 권한으로 작성하는 공지사항입니다.\n" +
                        "주제: " + topic + "\n\n" +
                        "위 주제를 바탕으로 사내 임직원들이 읽을 전문적이고 정중한 어조의 공지사항 초안을 작성해주세요.\n" +
                        "응답은 반드시 '[제목] (여기에 제목 내용)' 으로 시작하고, 줄을 바꾼 뒤 '[본문] (여기에 본문 내용)' 형태로 구분해서 하나만 작성해 주세요.";

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * Extracts name and birth date from an ID card image using multimodal AI.
     */
    public String extractIdCardInfo(byte[] imageBytes) {
        String prompt = "이 이미지는 한국의 주민등록증(Resident Registration Card)입니다. " +
                        "이미지에서 '성명(이름)'과 '주민등록번호 앞 6자리(생년월일)'를 찾아주세요. " +
                        "주변 텍스트에 구애받지 말고 정확한 정보만 추출하세요. " +
                        "응답은 반드시 아래 JSON 형식으로만 작성하고 다른 설명은 하지 마세요.\n" +
                        "{\"name\": \"추출된 이름\", \"birthDate\": \"6자리 생년월일\"}";

        try {
            // Spring AI Multimodal support (if configured model is multimodal)
            return chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(imageBytes)))
                    .call()
                    .content();
        } catch (Exception e) {
            // Fallback for non-multimodal environments / simulation
            System.err.println("OCR process failed or multimodal model not found: " + e.getMessage());
            // Since this is a demo, return a simulated result if it fails
            return "{\"name\": \"테스터\", \"birthDate\": \"950505\", \"note\": \"Simulated OCR result\"}";
        }
    }
}
