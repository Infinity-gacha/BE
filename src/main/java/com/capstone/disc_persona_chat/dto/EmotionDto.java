package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.SenderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class EmotionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String emotion;         // AI 응답의 감정
    }

}
