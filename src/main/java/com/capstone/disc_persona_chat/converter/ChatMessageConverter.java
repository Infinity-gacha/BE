package com.capstone.disc_persona_chat.converter;

import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.dto.ChatMessageDto;

/**
 * ChatMessage 엔티티와 ChatMessageDto 간의 변환을 위한 컨버터 인터페이스
 */
public interface ChatMessageConverter {
    
    /**
     * ChatMessage 엔티티를 ChatMessageDto.Response로 변환
     * @param entity ChatMessage 엔티티
     * @return 변환된 ChatMessageDto.Response
     */
    ChatMessageDto.Response toResponseDto(ChatMessage entity, Long personaId);
    
    /**
     * ChatMessage 엔티티를 ChatMessageDto.ContextMessage로 변환
     * @param entity ChatMessage 엔티티
     * @return 변환된 ChatMessageDto.ContextMessage
     */
    ChatMessageDto.ContextMessage toContextMessageDto(ChatMessage entity);
    
    /**
     * ChatMessageDto.Request를 ChatMessage 엔티티로 변환
     * @param dto ChatMessageDto.Request
     * @param personaId 메시지가 속한 페르소나 ID
     * @param senderType 발신자 유형
     * @return 변환된 ChatMessage 엔티티
     */
    ChatMessage toEntity(ChatMessageDto.Request dto, Long personaId, com.capstone.disc_persona_chat.Enums.SenderType senderType);
}
