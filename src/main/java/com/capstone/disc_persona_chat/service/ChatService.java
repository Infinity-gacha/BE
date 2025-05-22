package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.Enums.SenderType;
import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.exception.PersonaNotFoundException;
import com.capstone.disc_persona_chat.repository.ChatMessageRepository;
import com.capstone.disc_persona_chat.repository.ChatSummaryRepository;
import com.capstone.disc_persona_chat.repository.PersonaRepository;
import com.capstone.disc_persona_chat.converter.ChatMessageConverter;
import com.capstone.disc_persona_chat.converter.ChatSummaryConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j 
@Transactional(readOnly = true) 
public class ChatService {

    private final PersonaRepository personaRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSummaryRepository chatSummaryRepository;
    private final OpenAiIntegrationService openAiIntegrationService;
    
    // 컨버터 의존성 추가
    private final ChatMessageConverter chatMessageConverter;
    private final ChatSummaryConverter chatSummaryConverter;

    @Autowired
    public ChatService(
            PersonaRepository personaRepository,
            ChatMessageRepository chatMessageRepository,
            ChatSummaryRepository chatSummaryRepository,
            OpenAiIntegrationService openAiIntegrationService,
            ChatMessageConverter chatMessageConverter,
            ChatSummaryConverter chatSummaryConverter) {
        this.personaRepository = personaRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatSummaryRepository = chatSummaryRepository;
        this.openAiIntegrationService = openAiIntegrationService;
        this.chatMessageConverter = chatMessageConverter;
        this.chatSummaryConverter = chatSummaryConverter;
    }

    /**
     * 사용자 메시지를 처리하고 AI 응답을 생성하여 저장
     *
     * @param personaId 페르소나 ID
     * @param request   사용자 메시지 요청 DTO
     * @return AI 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional
    public ChatMessageDto.Response processMessage(Long personaId, ChatMessageDto.Request request) {
        // 1. 페르소나 조회 (없으면 예외 발생)
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        // 2. 사용자 메시지 저장 (컨버터 사용)
        ChatMessage userMessage = chatMessageConverter.toEntity(request, personaId, SenderType.USER);
        userMessage.setPersona(persona); // 연관관계 설정
        chatMessageRepository.save(userMessage);

        // 3. OpenAI에 보낼 대화 기록 준비 (최근 N개 또는 전체)
        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        List<ChatMessageDto.ContextMessage> historyContext = historyEntities.stream()
                .map(chatMessageConverter::toContextMessageDto)
                .collect(Collectors.toList());

        // 4. OpenAI API 호출하여 AI 응답 생성
        ChatMessageDto.Response aiResponseDto = openAiIntegrationService.generateChatResponse(persona, historyContext);

        // 5. AI 응답 메시지 저장
        ChatMessage aiMessage = ChatMessage.builder()
                .persona(persona)
                .content(aiResponseDto.getContent())
                .senderType(SenderType.AI)
                .emotion(aiResponseDto.getEmotion())
                .build();
        ChatMessage savedAiMessage = chatMessageRepository.save(aiMessage);

        // 6. 컨트롤러에 AI 응답 DTO 반환 (컨버터 사용)
        return chatMessageConverter.toResponseDto(savedAiMessage);
    }

    /**
     * 특정 페르소나와의 전체 채팅 기록을 조회
     *
     * @param personaId 페르소나 ID
     * @return 채팅 메시지 응답 DTO 목록
     */
    public List<ChatMessageDto.Response> getChatHistory(Long personaId) {
        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        return historyEntities.stream()
                .map(chatMessageConverter::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페르소나와의 대화 내용을 요약하고 저장
     *
     * @param personaId 페르소나 ID
     * @return 생성된 채팅 요약 응답 DTO, 요약 생성 실패 시 null
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional
    public ChatSummaryDto.Response generateAndSaveSummary(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        if (historyEntities.isEmpty()) {
            log.info("No chat history found for persona {}, cannot generate summary.", personaId);
            return null;
        }

        // 대화 기록을 하나의 문자열로 변환
        String conversationText = historyEntities.stream()
                .map(msg -> msg.getSenderType() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        // OpenAI API 호출하여 요약 및 분석 생성
        ChatSummaryDto.AnalysisResult analysisResult = openAiIntegrationService.generateSummaryAndAnalysis(persona, conversationText);

        if (analysisResult != null) {
            // 컨버터를 사용하여 DTO를 엔티티로 변환하고 저장
            ChatSummary summary = chatSummaryConverter.toEntity(analysisResult, persona);
            ChatSummary savedSummary = chatSummaryRepository.save(summary);
            
            // 컨버터를 사용하여 엔티티를 DTO로 변환하여 반환
            return chatSummaryConverter.toResponseDto(savedSummary);
        } else {
            log.error("Failed to generate summary for persona {}", personaId);
            return null;
        }
    }

    /**
     * 특정 페르소나의 가장 최신 채팅 요약을 조회
     *
     * @param personaId 페르소나 ID
     * @return 최신 채팅 요약 응답 DTO, 요약이 없으면 null
     */
    public ChatSummaryDto.Response getLatestChatSummary(Long personaId) {
        return chatSummaryRepository.findTopByPersonaIdOrderByTimestampDesc(personaId)
                .map(chatSummaryConverter::toResponseDto)
                .orElse(null);
    }

    /**
     * 특정 페르소나의 모든 채팅 요약 기록을 조회
     *
     * @param personaId 페르소나 ID
     * @return 채팅 요약 응답 DTO 목록 (최신순)
     */
    public List<ChatSummaryDto.Response> getAllChatSummaries(Long personaId) {
        List<ChatSummary> summaries = chatSummaryRepository.findByPersonaIdOrderByTimestampDesc(personaId);
        return summaries.stream()
                .map(chatSummaryConverter::toResponseDto)
                .collect(Collectors.toList());
    }
}
