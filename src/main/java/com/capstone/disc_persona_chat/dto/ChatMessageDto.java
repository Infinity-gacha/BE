package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.SenderType;
import com.capstone.disc_persona_chat.domain.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String message; // 사용자가 보낸 메시지 내용
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;                // 메시지 ID
        private Long personaId;         // 관련 페르소나 ID
        private String content;       // 메시지 내용
        private SenderType senderType;  // 발신자 유형 (USER 또는 AI)
        private String emotion;         // AI 응답의 감정
        private LocalDateTime timestamp; // 메시지 타임스탬프

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextMessage {
        private String role;    // OpenAI API에 사용될 역할 ("user" 또는 "assistant")
        private String content; // 메시지 내용

    }


    
  
}
