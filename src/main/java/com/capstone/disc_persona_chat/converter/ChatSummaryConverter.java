package com.capstone.disc_persona_chat.converter;

import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;

/**
 * ChatSummary 엔티티와 ChatSummaryDto 간의 변환을 위한 컨버터 인터페이스
 */
public interface ChatSummaryConverter {
    
    /**
     * ChatSummary 엔티티를 ChatSummaryDto.Response로 변환
     * @param entity ChatSummary 엔티티
     * @return 변환된 ChatSummaryDto.Response
     */
    ChatSummaryDto.Response toResponseDto(ChatSummary entity, Long personaId);
    
    /**
     * ChatSummaryDto.AnalysisResult를 ChatSummary 엔티티로 변환
     * @param dto ChatSummaryDto.AnalysisResult
     * @return 변환된 ChatSummary 엔티티
     */
    ChatSummary toEntity(ChatSummaryDto.AnalysisResult dto, UserPersona userPersona);
    
    /**
     * 긴 텍스트 필드를 필요에 따라 잘라내는 유틸리티 메서드
     * @param value 원본 텍스트
     * @param maxLength 최대 길이
     * @return 최대 길이로 잘린 텍스트
     */
    String truncateIfNecessary(String value, int maxLength);
}
