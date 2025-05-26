package com.capstone.disc_persona_chat.converter.impl;

import com.capstone.disc_persona_chat.converter.ChatSummaryConverter;
import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ChatSummaryConverter 인터페이스의 구현 클래스
 */
@Component
public class ChatSummaryConverterImpl implements ChatSummaryConverter {

    /**
     * ChatSummary 엔티티를 ChatSummaryDto.Response로 변환
     * @param entity ChatSummary 엔티티
     * @return 변환된 ChatSummaryDto.Response
     */
    @Override
    public ChatSummaryDto.Response toResponseDto(ChatSummary entity, Long personaId) {
        if (entity == null) {
            return null;
        }
        
        return ChatSummaryDto.Response.builder()
                .id(entity.getId())
                .personaId(personaId)
                .summaryText(entity.getSummaryText())
                .score(entity.getScore())
                .corePoints(truncateIfNecessary(entity.getCorePoints(), 65535))
                .improvements(truncateIfNecessary(entity.getImprovements(), 65535))
                .tips(truncateIfNecessary(entity.getTips(), 65535))
                .timestamp(entity.getTimestamp())
                .build();
    }
    
    /**
     * ChatSummaryDto.AnalysisResult를 ChatSummary 엔티티로 변환
     * @param dto ChatSummaryDto.AnalysisResult
     * @param userPersona 사용자 페르소나 객체
     * @return 변환된 ChatSummary 엔티티
     */
    @Override
    public ChatSummary toEntity(ChatSummaryDto.AnalysisResult dto, UserPersona userPersona) {
        if (dto == null) {
            return null;
        }
        
        return ChatSummary.builder()
                .userPersona(userPersona)  // UserPersona 설정
                .persona(userPersona.getPersona())  // UserPersona에서 Persona를 가져와 설정
                .summaryText(dto.getSummaryText())
                .score(dto.getScore())
                .corePoints(dto.getCorePoints())
                .improvements(dto.getImprovements())
                .tips(dto.getTips())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 긴 텍스트 필드를 필요에 따라 잘라내는 유틸리티 메서드
     * @param value 원본 텍스트
     * @param maxLength 최대 길이
     * @return 최대 길이로 잘린 텍스트
     */
    @Override
    public String truncateIfNecessary(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
