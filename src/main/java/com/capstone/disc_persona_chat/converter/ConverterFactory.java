package com.capstone.disc_persona_chat.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 컨버터 팩토리 클래스
 * 모든 컨버터 인스턴스에 대한 중앙 접근점을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final ChatMessageConverter chatMessageConverter;
    private final UserConverter userConverter;
    private final PersonaConverter personaConverter;
    private final ChatSummaryConverter chatSummaryConverter;

    /**
     * ChatMessage 컨버터 인스턴스 반환
     * @return ChatMessageConverter 인스턴스
     */
    public ChatMessageConverter getChatMessageConverter() {
        return chatMessageConverter;
    }

    /**
     * User 컨버터 인스턴스 반환
     * @return UserConverter 인스턴스
     */
    public UserConverter getUserConverter() {
        return userConverter;
    }

    /**
     * Persona 컨버터 인스턴스 반환
     * @return PersonaConverter 인스턴스
     */
    public PersonaConverter getPersonaConverter() {
        return personaConverter;
    }

    /**
     * ChatSummary 컨버터 인스턴스 반환
     * @return ChatSummaryConverter 인스턴스
     */
    public ChatSummaryConverter getChatSummaryConverter() {
        return chatSummaryConverter;
    }
}
