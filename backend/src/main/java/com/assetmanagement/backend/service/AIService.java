package com.assetmanagement.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

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
}
