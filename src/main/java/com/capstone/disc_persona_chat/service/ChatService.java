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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor 
@Slf4j 
@Transactional(readOnly = true) 
public class ChatService {

    private final PersonaRepository personaRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSummaryRepository chatSummaryRepository;
    private final OpenAiIntegrationService openAiIntegrationService;

    /**
     * 사용자 메시지를 처리하고 AI 응답을 생성하여 저장
     *
     * @param personaId 페르소나 ID
     * @param request   사용자 메시지 요청 DTO
     * @return AI 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional // 쓰기 작업이 있으므로 클래스 레벨의 readOnly 설정을 오버라이드
    public ChatMessageDto.Response processMessage(Long personaId, ChatMessageDto.Request request) {
        // 1. 페르소나 조회 (없으면 예외 발생)
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .persona(persona)
                .content(request.getMessage())
                .senderType(SenderType.USER)
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(userMessage);
        // persona.addChatMessage(userMessage); // 양방향 관계 설정 (선택 사항, JPA가 관리)

        // 3. OpenAI에 보낼 대화 기록 준비 (최근 N개 또는 전체)
        // 예시: 전체 기록 사용
        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        List<ChatMessageDto.ContextMessage> historyContext = historyEntities.stream()
                .map(ChatMessageDto.ContextMessage::fromEntity)
                .collect(Collectors.toList());

        // 4. OpenAI API 호출하여 AI 응답 생성
        ChatMessageDto.Response aiResponseDto = openAiIntegrationService.generateChatResponse(persona, historyContext);

        // 5. AI 응답 메시지 저장
        ChatMessage aiMessage = ChatMessage.builder()
                .persona(persona)
                .content(aiResponseDto.getContent())
                .senderType(SenderType.AI)
                .emotion(aiResponseDto.getEmotion()) // 감정 정보 저장
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(aiMessage);
        // persona.addChatMessage(aiMessage); // 양방향 관계 설정 (선택 사항)

        // 6. 컨트롤러에 AI 응답 DTO 반환 (저장된 AI 메시지 기반으로 다시 생성하거나 기존 DTO 사용)
        // 여기서는 새로 저장된 aiMessage 기반으로 ID와 타임스탬프가 포함된 최종 DTO를 반환
        return ChatMessageDto.Response.fromEntity(aiMessage);
    }

    /**
     * 특정 페르소나와의 전체 채팅 기록을 조회
     *
     * @param personaId 페르소나 ID
     * @return 채팅 메시지 응답 DTO 목록
     */
    public List<ChatMessageDto.Response> getChatHistory(Long personaId) {
        // 페르소나 존재 여부 확인 (선택 사항, 필요 시 추가)
        // if (!personaRepository.existsById(personaId)) {
        //     throw new PersonaNotFoundException("Persona not found with id: " + personaId);
        // }
        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        return historyEntities.stream()
                .map(ChatMessageDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페르소나와의 대화 내용을 요약하고 저장
     *
     * @param personaId 페르소나 ID
     * @return 생성된 채팅 요약 응답 DTO, 요약 생성 실패 시 null
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional // 쓰기 작업
    public ChatSummaryDto.Response generateAndSaveSummary(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        List<ChatMessage> historyEntities = chatMessageRepository.findByPersonaIdOrderByTimestampAsc(personaId);
        if (historyEntities.isEmpty()) {
            log.info("No chat history found for persona {}, cannot generate summary.", personaId);
            return null; // 채팅 기록 없으면 요약 생성 불가
        }

        // 대화 기록을 하나의 문자열로 변환 (또는 다른 형식)
        String conversationText = historyEntities.stream()
                .map(msg -> msg.getSenderType() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        // OpenAI API 호출하여 요약 및 분석 생성
        ChatSummaryDto.AnalysisResult analysisResult = openAiIntegrationService.generateSummaryAndAnalysis(persona, conversationText);

        if (analysisResult != null) {
            // 분석 결과를 ChatSummary 엔티티로 변환하여 저장
            ChatSummary summary = analysisResult.toEntity(persona);
            ChatSummary savedSummary = chatSummaryRepository.save(summary);
            // persona.addChatSummary(savedSummary); // 양방향 관계 설정 (선택 사항)
            return ChatSummaryDto.Response.fromEntity(savedSummary);
        } else {
            log.error("Failed to generate summary for persona {}", personaId);
            return null; // 요약 생성 실패
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
                .map(ChatSummaryDto.Response::fromEntity)
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
                .map(ChatSummaryDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}

