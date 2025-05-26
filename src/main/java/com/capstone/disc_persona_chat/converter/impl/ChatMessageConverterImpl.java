package com.capstone.disc_persona_chat.converter.impl;

import com.capstone.disc_persona_chat.Enums.SenderType;
import com.capstone.disc_persona_chat.converter.ChatMessageConverter;
import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import org.springframework.stereotype.Component;

/**
 * ChatMessageConverter 인터페이스의 구현 클래스
 */
@Component
public class ChatMessageConverterImpl implements ChatMessageConverter {

    /**
     * ChatMessage 엔티티를 ChatMessageDto.Response로 변환
     * @param entity ChatMessage 엔티티
     * @return 변환된 ChatMessageDto.Response
     */
    @Override
    public ChatMessageDto.Response toResponseDto(ChatMessage entity, Long personaId) {
        if (entity == null) {
            return null;
        }
        
        return ChatMessageDto.Response.builder()
                .id(entity.getId())
                .personaId(personaId)
                .content(entity.getContent())
                .senderType(entity.getSenderType())
                .emotion(entity.getEmotion())
                .timestamp(entity.getTimestamp())
                .build();
    }

    /**
     * ChatMessage 엔티티를 ChatMessageDto.ContextMessage로 변환
     * @param entity ChatMessage 엔티티
     * @return 변환된 ChatMessageDto.ContextMessage
     */
    @Override
    public ChatMessageDto.ContextMessage toContextMessageDto(ChatMessage entity) {
        if (entity == null) {
            return null;
        }
        
        String role = entity.getSenderType() == SenderType.USER ? "user" : "assistant";
        return ChatMessageDto.ContextMessage.builder()
                .role(role)
                .content(entity.getContent())
                .build();
    }

    /**
     * ChatMessageDto.Request를 ChatMessage 엔티티로 변환
     * @param dto ChatMessageDto.Request
     * @param personaId 메시지가 속한 페르소나 ID
     * @param senderType 발신자 유형
     * @return 변환된 ChatMessage 엔티티
     */
    @Override
    public ChatMessage toEntity(ChatMessageDto.Request dto, Long personaId, SenderType senderType) {

        if (dto == null) {
            return null;
        }
        
        // 실제 구현에서는 Persona 객체를 찾아서 설정해야 함
        // 서비스 레이어에서 Persona 객체를 설정해야 함
        return ChatMessage.builder()
                .content(dto.getMessage())
                .senderType(senderType)
                // Persona는 서비스 레이어에서 설정
                .build();
    }
}
