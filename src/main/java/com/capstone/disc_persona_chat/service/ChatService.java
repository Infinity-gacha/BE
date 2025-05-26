package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.Enums.SenderType;
import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.exception.PersonaNotFoundException;
import com.capstone.disc_persona_chat.exception.UnauthorizedAccessException;
import com.capstone.disc_persona_chat.repository.ChatMessageRepository;
import com.capstone.disc_persona_chat.repository.ChatSummaryRepository;
import com.capstone.disc_persona_chat.repository.PersonaRepository;
import com.capstone.disc_persona_chat.converter.ChatMessageConverter;
import com.capstone.disc_persona_chat.converter.ChatSummaryConverter;
import com.capstone.disc_persona_chat.repository.UserPersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j 
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    private final PersonaRepository personaRepository;
    private final UserPersonaRepository userPersonaRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSummaryRepository chatSummaryRepository;
    private final OpenAiIntegrationService openAiIntegrationService;
    
    private final ChatMessageConverter chatMessageConverter;
    private final ChatSummaryConverter chatSummaryConverter;

    /**
     * 사용자 메시지를 처리하고 AI 응답을 생성하여 저장 (사용자 권한 검증 포함)
     *
     * @param personaId 페르소나 ID
     * @param request 사용자 메시지 요청 DTO
     * @param userId 현재 로그인한 사용자 ID
     * @return AI 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    @Transactional
    public ChatMessageDto.Response processMessageWithUserCheck(Long personaId, ChatMessageDto.Request request, Long userId) {
        // 1. 페르소나 조회 및 사용자 권한 검증
        UserPersona userPersona = userPersonaRepository.findByUserIdAndPersonaId(userId, personaId)
                .orElseThrow(() -> new UnauthorizedAccessException("User does not have access to this persona"));

        Persona persona = userPersona.getPersona();

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = chatMessageConverter.toEntity(request, personaId, SenderType.USER);
        userPersona.addChatMessage(userMessage); // 사용자 메시지를 유저 페르소나의 ChatMessage 리스트에 추가
        chatMessageRepository.save(userMessage); // 유저 메세지 데베에 추가

        // 3. OpenAI에 보낼 대화 기록 준비
        List<ChatMessage> historyEntities = chatMessageRepository.findByUserPersonaIdOrderByTimestampAsc(userPersona.getId());
        List<ChatMessageDto.ContextMessage> historyContext = historyEntities.stream()
                .map(chatMessageConverter::toContextMessageDto)
                .collect(Collectors.toList());

        // 4. OpenAI API 호출하여 AI 응답 생성
        ChatMessageDto.Response aiResponseDto = openAiIntegrationService.generateChatResponse(persona, historyContext);

        // 5. AI 응답 메시지 저장
        ChatMessage aiMessage = ChatMessage.builder()
                .userPersona(userPersona)
                .content(aiResponseDto.getContent())
                .senderType(SenderType.AI)
                .emotion(aiResponseDto.getEmotion())
                .build();
        ChatMessage savedAiMessage = chatMessageRepository.save(aiMessage);

        // 6. AI 응답 DTO 반환
        return chatMessageConverter.toResponseDto(savedAiMessage, personaId);
    }

    /**
     * 특정 페르소나와의 전체 채팅 기록을 조회 (사용자 권한 검증 포함)
     *
     * @param personaId 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 채팅 메시지 응답 DTO 목록
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    public List<ChatMessageDto.Response> getChatHistoryWithUserCheck(Long personaId, Long userId) {
        // 페르소나 조회 및 사용자 권한 검증
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        // 현재 사용자가 페르소나의 소유자인지 확인
        UserPersona userPersona = userPersonaRepository.findByUserIdAndPersonaId(userId, personaId)
                .orElseThrow(() -> new UnauthorizedAccessException("User does not have access to this persona"));

        // 채팅 기록 조회 및 변환
        List<ChatMessage> historyEntities = chatMessageRepository.findByUserPersonaIdOrderByTimestampAsc(userPersona.getId());
        return historyEntities.stream()
                .map(chatMessage -> chatMessageConverter.toResponseDto(chatMessage, personaId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 페르소나와의 대화 내용을 요약하고 저장 (사용자 권한 검증 포함)
     *
     * @param personaId 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 생성된 채팅 요약 응답 DTO, 요약 생성 실패 시 null
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    @Transactional
    public ChatSummaryDto.Response generateAndSaveSummaryWithUserCheck(Long personaId, Long userId) {
        // 페르소나 조회 및 사용자 권한 검증
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));
        
        // 현재 사용자가 페르소나의 소유자인지 확인
        UserPersona userPersona = userPersonaRepository.findByUserIdAndPersonaId(userId, personaId)
                .orElseThrow(() -> new UnauthorizedAccessException("User does not have access to this persona"));

        List<ChatMessage> historyEntities = chatMessageRepository.findByUserPersonaIdOrderByTimestampAsc(userPersona.getId());

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
            // 요약 저장 및 변환 - UserPersona 파라미터 추가
            ChatSummary summary = chatSummaryConverter.toEntity(analysisResult, userPersona);
            ChatSummary savedSummary = chatSummaryRepository.save(summary);
            return chatSummaryConverter.toResponseDto(savedSummary, personaId);
        } else {
            log.error("Failed to generate summary for persona {}", personaId);
            return null;
        }
    }

    /**
     * 특정 페르소나의 가장 최신 채팅 요약을 조회 (사용자 권한 검증 포함)
     *
     * @param personaId 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 최신 채팅 요약 응답 DTO, 요약이 없으면 null
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    public ChatSummaryDto.Response getLatestChatSummaryWithUserCheck(Long personaId, Long userId) {
        // 페르소나 조회 및 사용자 권한 검증
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));
        
        // 현재 사용자가 페르소나의 소유자인지 확인
        UserPersona userPersona = userPersonaRepository.findByUserIdAndPersonaId(userId, personaId)
                .orElseThrow(() -> new UnauthorizedAccessException("User does not have access to this persona"));

        // 최신 요약 조회 및 변환
        return chatSummaryRepository.findFirstByUserPersonaIdOrderByTimestampDesc(userPersona.getId())
                .map(summary -> chatSummaryConverter.toResponseDto(summary, personaId))
                .orElse(null);
    }

    /**
     * 특정 페르소나의 모든 채팅 요약 기록을 조회 (사용자 권한 검증 포함)
     *
     * @param personaId 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 채팅 요약 응답 DTO 목록 (최신순)
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    public List<ChatSummaryDto.Response> getAllChatSummariesWithUserCheck(Long personaId, Long userId) {
        // 페르소나 조회 및 사용자 권한 검증
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + personaId));

        // 현재 사용자가 페르소나의 소유자인지 확인
        UserPersona userPersona = userPersonaRepository.findByUserIdAndPersonaId(userId, personaId)
                .orElseThrow(() -> new UnauthorizedAccessException("User does not have access to this persona"));
        
        // 모든 요약 조회 및 변환
        List<ChatSummary> summaries = chatSummaryRepository.findByUserPersonaIdOrderByTimestampDesc(userPersona.getId());
        return summaries.stream()
                .map(chatSummaries -> chatSummaryConverter.toResponseDto(chatSummaries, personaId))
                .collect(Collectors.toList());
    }
}
