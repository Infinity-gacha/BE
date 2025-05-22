package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.SenderType;
import com.capstone.disc_persona_chat.domain.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

        public static Response fromEntity(ChatMessage message) {
            return Response.builder()
                    .id(message.getId())
                    .personaId(message.getPersona().getId())
                    .content(message.getContent())
                    .senderType(message.getSenderType())
                    .emotion(message.getEmotion()) // AI 메시지의 경우 감정 포함
                    .timestamp(message.getTimestamp())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextMessage {
        private String role;    // OpenAI API에 사용될 역할 ("user" 또는 "assistant")
        private String content; // 메시지 내용

        // ChatMessage 엔티티를 ContextMessage DTO로 변환
        public static ContextMessage fromEntity(ChatMessage message) {
            String role = message.getSenderType() == SenderType.USER ? "user" : "assistant";
            return ContextMessage.builder()
                    .role(role)
                    .content(message.getContent())
                    .build();
        }
    }
}
