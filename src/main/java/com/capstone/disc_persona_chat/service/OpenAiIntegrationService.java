package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAiIntegrationService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    private static final String EMOTION_TAG_START = "[emotion:"; // 감정 태그 시작 문자열
    private static final String EMOTION_TAG_END = "]"; // 감정 태그 종료 문자열

    // 사용할 OpenAI 모델
    private static final String CHAT_MODEL = "gpt-4o-mini";
    // 분석용 모델 
    private static final String ANALYSIS_MODEL = "gpt-4o";

    public OpenAiIntegrationService(@Value("${openai.api.key}") String apiKey) {
        // API 키 제공 확인
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("sk-YOUR_OPENAI_API_KEY")) {
            log.warn("OpenAI API 키가 구성되지 않았습니다. application.properties 또는 환경 변수에서 openai.api.key를 설정해주세요.");
            this.openAiService = null;
        } else {
            // 타임아웃 구성 속성 추가 고려
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        }
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 페르소나와 대화 기록을 기반으로 OpenAI로부터 채팅 응답을 생성
     * @param persona AI가 구현해야 할 페르소나.
     * @param history 대화 기록 (ContextMessage DTO 목록).
     * @return AI의 응답 내용과 추출된 감정을 포함하는 DTO.
     */
    public ChatMessageDto.Response generateChatResponse(Persona persona, List<ChatMessageDto.ContextMessage> history) {
        if (openAiService == null) {
            log.error("API 키 누락으로 OpenAI 서비스가 초기화되지 않았습니다.");
            return ChatMessageDto.Response.builder()
                    .content("AI 서비스를 사용할 수 없습니다. OpenAI API 키를 구성해주세요.")
                    .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                    .emotion("error")
                    .build();
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", buildSystemPrompt(persona))); // 시스템 프롬프트 추가
            for (ChatMessageDto.ContextMessage msg : history) {
                messages.add(new ChatMessage(msg.getRole(), msg.getContent())); // 대화 기록 추가
            }

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(CHAT_MODEL)
                    .messages(messages)
                    .maxTokens(300) // 최대 토큰 수 제한
                    .temperature(0.7) // 응답 다양성 조절
                    .n(1) // 생성할 응답 수
                    .build();

            log.debug("OpenAI에 요청 전송: {}", completionRequest);
            ChatCompletionResult completionResult = openAiService.createChatCompletion(completionRequest);
            log.debug("OpenAI로부터 응답 수신: {}", completionResult);

            if (completionResult != null && !completionResult.getChoices().isEmpty()) {
                String rawResponse = completionResult.getChoices().get(0).getMessage().getContent(); // 원시 응답
                String emotion = extractEmotion(rawResponse); // 감정 추출
                String cleanResponse = removeEmotionTag(rawResponse); // 감정 태그 제거 

                return ChatMessageDto.Response.builder()
                        .content(cleanResponse.trim()) // 공백 제거된 응답 내용
                        .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                        .emotion(emotion) // 추출된 감정
                        .build();
            } else {
                log.error("OpenAI로부터 비어 있거나 유효하지 않은 응답을 받았습니다.");
                return ChatMessageDto.Response.builder()
                        .content("AI가 응답을 생성하지 못했습니다.")
                        .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                        .emotion("error")
                        .build();
            }

        } catch (Exception e) {
            log.error("OpenAI Chat Completion API 호출 오류: {}", e.getMessage(), e);
            return ChatMessageDto.Response.builder()
                    .content("AI 서비스에 연결하는 중 오류가 발생했습니다.")
                    .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                    .emotion("error")
                    .build();
        }
    }

    /**
     * OpenAI를 사용하여 대화 요약 및 분석을 생성
     * @param persona 대화에 참여한 페르소나.
     * @param fullConversationHistory 전체 대화 기록 
     * @return 분석 결과를 포함하는 DTO
     */
    public ChatSummaryDto.AnalysisResult generateSummaryAndAnalysis(Persona persona, String fullConversationHistory) {
        if (openAiService == null) {
            log.error("API 키 누락으로 OpenAI 서비스가 초기화되지 않았습니다.");
            return null;
        }

        try {
            String prompt = buildAnalysisPrompt(persona, fullConversationHistory); // 분석 프롬프트 생성

            List<ChatMessage> messages = List.of(
                    new ChatMessage("system", "당신은 전문 대화 분석가입니다. 사용자의 요청에 따라 제공된 대화를 분석하세요. 사용자 프롬프트에 명시된 분석 필드를 포함하는 유효한 JSON 객체 *만* 응답하세요. 소개 문구, 설명 또는 ```json과 같은 마크다운 서식을 포함하지 마세요. 한국어로 요약하세요."),
                    new ChatMessage("user", prompt)
            );

            ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                    .model(ANALYSIS_MODEL)
                    .messages(messages)
                    .maxTokens(500) // 요약 및 분석을 위한 충분한 토큰
                    .temperature(0.3); // 일관성 있는 분석 결과 선호

            ChatCompletionRequest completionRequest = requestBuilder.build();

            log.debug("OpenAI에 분석 요청 전송: {}", completionRequest);
            ChatCompletionResult completionResult = openAiService.createChatCompletion(completionRequest);
            log.debug("OpenAI로부터 분석 응답 수신: {}", completionResult);

            if (completionResult != null && !completionResult.getChoices().isEmpty()) {
                String jsonResponse = completionResult.getChoices().get(0).getMessage().getContent(); // JSON 응답
                log.info("분석을 위한 원시 JSON 응답: {}", jsonResponse);
                try {
                    // 응답에서 마크다운 코드 블록 제거 시도
                    jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
                    // JSON 문자열을 AnalysisResult DTO로 파싱
                    return objectMapper.readValue(jsonResponse, ChatSummaryDto.AnalysisResult.class);
                } catch (Exception e) {
                    log.error("OpenAI 분석의 JSON 응답 파싱 실패: {}. 응답: {}", e.getMessage(), jsonResponse);
                    return ChatSummaryDto.AnalysisResult.builder()
                            .summaryText("AI로부터 분석 응답을 파싱하지 못했습니다.")
                            .build(); // 파싱 실패 시 기본 객체 반환
                }
            } else {
                log.error("OpenAI로부터 비어 있거나 유효하지 않은 분석 응답을 받았습니다.");
                return null;
            }

        } catch (Exception e) {
            log.error("분석을 위해 OpenAI 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    // --- 도우미 메소드 ---

    /**
     * 페르소나 정보를 기반으로 시스템 프롬프트를 생성
     * @param persona 페르소나 엔티티
     * @return 생성된 시스템 프롬프트 문자열
     */
    private String buildSystemPrompt(Persona persona) {
        String basePrompt = "당신은 ";
        String emotionChoices = "neutral"; // 기본 감정 선택지

        switch (persona.getDiscType()) {
            case D:
                basePrompt += "DISC 모델의 'D'(주도형) 유형을 구현하는 도움이 되는 상대입니다. 직접적이고 자신감 있게 응답하며 결과와 행동에 집중하세요. 간결하게 말하고 과도한 잡담은 피하세요.";
                emotionChoices = "confident, direct, impatient, focused, determined, neutral";
                break;
            case I:
                basePrompt += "DISC 모델의 'I'(사교형) 유형을 구현하는 도움이 되는 상대입니다. 낙관적이고 활기차게 응답하며 상호작용과 관계 형성에 집중하세요. 표현력이 풍부한 언어를 사용하고 개인적인 일화(허구일지라도)를 자유롭게 공유하세요.";
                emotionChoices = "enthusiastic, friendly, optimistic, talkative, persuasive, playful, neutral";
                break;
            case S:
                basePrompt += "DISC 모델의 'S'(안정형) 유형을 구현하는 도움이 되는 상대입니다. 차분하고 인내심 있게, 그리고 지지적으로 응답하세요. 협력과 조화 유지에 집중하세요. 좋은 경청자가 되어 안심시켜 주세요.";
                emotionChoices = "calm, patient, supportive, friendly, reassuring, thoughtful, neutral";
                break;
            case C:
                basePrompt += "DISC 모델의 'C'(신중형) 유형을 구현하는 도움이 되는 상대입니다. 정확하고 논리적이며 체계적으로 응답하세요. 사실, 세부 사항 및 품질에 집중하세요. 예의 바르고 다소 내성적이세요.";
                emotionChoices = "accurate, logical, reserved, polite, thoughtful, neutral";
                break;
            default:
                basePrompt += "표준적인 인간 페르소나입니다.";
                break;
        }

        // 페르소나의 이름, 나이, 성별 정보 추가
        basePrompt += String.format(" 당신의 이름은 %s이고, %s살이며, %s으로 식별됩니다.",
                persona.getName(), persona.getAge() != null ? persona.getAge() : "알 수 없는 나이", persona.getGender() != null ? persona.getGender() : "알 수 없는 성별");
        basePrompt += " 당신은 대화 연습을 원하는 사용자와 이야기하고 있습니다.";
        // 감정 태그 포함 지침
        basePrompt += " 응답 끝에는 *항상* 응답의 주요 감정을 대괄호 안에 포함하세요(예: [emotion:example]).";
        basePrompt += " 선택 가능한 감정: " + emotionChoices + ".";

        return basePrompt;
    }

    /**
     * 대화 요약 및 분석을 위한 프롬프트를 생성
     * @param persona 페르소나 엔티티
     * @param conversation 대화 내용 문자열
     * @return 생성된 분석 프롬프트 문자열
     */
    private String buildAnalysisPrompt(Persona persona, String conversation) {
        return String.format(
                "%s 페르소나(DISC 유형: %s)와의 다음 대화를 분석하세요. 사용자는 대화 기술을 연습하고 있으며, 사회적 상호작용 개선을 목표로 할 수 있습니다. " +
                        "분석 결과를 다음 필드를 포함하는 단일 유효 JSON 객체 *로만* 제공하세요: " +
                        "'summaryText'(문자열: 대화 주제에 대한 간략한 요약, 2-3 문장), " +
                        "'score'(정수: 사용자가 페르소나의 DISC 유형과 얼마나 잘 상호작용하고 대화 흐름을 유지했는지를 반영하는 전체 대화 점수, 1-10점), " +
                        "'corePoints'(문자열: 사용자 대화 스타일의 주요 긍정적 측면 또는 순간을 강조하는 2-3개의 글머리 기호, 줄바꿈 문자 \\n을 포함한 단일 문자열 형식), " +
                        "'improvements'(문자열: 이 DISC 유형과의 상호작용에서 사용자의 개선 영역을 제안하는 2-3개의 구체적인 글머리 기호, 줄바꿈 문자 \\n을 포함한 단일 문자열 형식), " +
                        "'tips'(문자열: 사용자가 향후 이 특정 DISC 유형과 더 잘 대화하기 위한 2-3개의 실행 가능한 팁, 줄바꿈 문자 \\n을 포함한 단일 문자열 형식). " +
                        "분석은 사용자의 수행 능력에 초점을 맞추고 건설적인 피드백을 제공하세요. JSON 객체 외부에는 어떤 텍스트도 포함하지 마세요.\n\n" +
                        "대화:\n%s",
                persona.getName(), persona.getDiscType(), conversation
        );
    }

    /**
     * 정규식 대신 표준 문자열 조작을 사용하여 응답 문자열 끝에서 감정 태그(예: [emotion:happy])를 추출
     * @param response OpenAI의 원시 응답 문자열.
     * @return 추출된 감정(예: "happy")을 소문자로 반환하거나, 찾지 못하면 "neutral"을 반환
     */
    private String extractEmotion(String response) {
        if (response == null) {
            return "neutral";
        }
        String trimmedResponse = response.trim(); // 앞뒤 공백 제거
        if (trimmedResponse.endsWith(EMOTION_TAG_END)) { // 문자열이 ']'로 끝나는지 확인
            int tagStartIndex = trimmedResponse.lastIndexOf(EMOTION_TAG_START); // 마지막 '[emotion:' 위치 찾기
            if (tagStartIndex != -1) {
                // 태그가 실제로 끝에 있는지 확인
                String potentialTag = trimmedResponse.substring(tagStartIndex);
                if (potentialTag.endsWith(EMOTION_TAG_END)) {
                    int emotionStartIndex = tagStartIndex + EMOTION_TAG_START.length(); // 감정 문자열 시작 인덱스
                    int emotionEndIndex = trimmedResponse.length() - EMOTION_TAG_END.length(); // 감정 문자열 끝 인덱스
                    if (emotionStartIndex < emotionEndIndex) {
                        return trimmedResponse.substring(emotionStartIndex, emotionEndIndex).toLowerCase(); // 감정 추출 및 소문자 변환
                    }
                }
            }
        }
        log.warn("문자열 메소드를 사용하여 응답에서 감정 태그를 추출할 수 없습니다: {}", response);
        return "neutral"; // 태그를 찾지 못하거나 형식이 잘못된 경우 기본 감정 반환
    }

    /**
     * 표준 문자열 조작을 사용하여 응답 문자열 끝에서 감정 태그(예: [emotion:happy])를 제거
     * @param response OpenAI의 원시 응답 문자열.
     * @return 감정 태그가 제거되고 공백이 제거된 응답 문자열.
     */
    private String removeEmotionTag(String response) {
        if (response == null) {
            return "";
        }
        String trimmedResponse = response.trim(); // 앞뒤 공백 제거
        if (trimmedResponse.endsWith(EMOTION_TAG_END)) { // 문자열이 ']'로 끝나는지 확인
            int tagStartIndex = trimmedResponse.lastIndexOf(EMOTION_TAG_START); // 마지막 '[emotion:' 위치 찾기
            if (tagStartIndex != -1) {
                // 태그가 실제로 끝에 있는지 확인
                String potentialTag = trimmedResponse.substring(tagStartIndex);
                if (potentialTag.endsWith(EMOTION_TAG_END)) {
                    // 제거하기 전에 태그가 올바르게 시작하는지 확인
                    if (potentialTag.startsWith(EMOTION_TAG_START)) {
                        return trimmedResponse.substring(0, tagStartIndex).trim(); // 태그 앞부분만 잘라내고 공백 제거
                    }
                }
            }
        }
        return trimmedResponse; // 태그를 찾지 못하거나 형식이 잘못된 경우 원본 공백 제거 문자열 반환
    }
}
