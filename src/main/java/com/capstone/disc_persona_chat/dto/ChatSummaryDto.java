package com.capstone.disc_persona_chat.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatSummaryDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;                // 요약 ID
        private Long personaId;         // 관련 페르소나 ID
        private String summaryText;     // 대화 요약 텍스트
        private Integer score;          // 대화 점수 (예: 1-10)
        private String corePoints;      // 핵심 긍정 포인트 
        private String improvements;    // 개선점 
        private String tips;            // 대화 팁 
        private LocalDateTime timestamp; // 요약 생성 타임스탬프
    }

    // OpenAI 분석 응답을 직접 매핑하기 위한 내부 클래스
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisResult {
        private String summaryText;
        private Integer score;
        private String corePoints;
        private String improvements;
        private String tips;
    }
}
