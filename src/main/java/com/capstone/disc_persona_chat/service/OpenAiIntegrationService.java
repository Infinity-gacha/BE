package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.Enums.DiscType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class OpenAiIntegrationService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String personaJsonPath;
    
    // 페르소나 JSON 파일 캐싱을 위한 맵
    private final Map<String, JsonNode> personaCache = new HashMap<>();

    // 감정 태그 관련 상수
    private static final String EMOTION_TAG_START = "[감정:"; // 감정 태그 시작 문자열 (한글로 변경)
    private static final String EMOTION_TAG_END = "]"; // 감정 태그 종료 문자열
    


    // 사용할 OpenAI 모델
    private static final String CHAT_MODEL = "gpt-4o";
    // 분석용 모델 
    private static final String ANALYSIS_MODEL = "gpt-4o";

    public OpenAiIntegrationService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${persona.json.path:classpath:personas/}") String personaJsonPath,
            ResourceLoader resourceLoader) {
        // API 키 제공 확인
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("sk-YOUR_OPENAI_API_KEY")) {
            log.warn("OpenAI API 키가 구성되지 않았습니다. application.properties 또는 환경 변수에서 openai.api.key를 설정해주세요.");
            this.openAiService = null;
        } else {
            // 타임아웃 구성 속성 추가 고려
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        }
        this.objectMapper = new ObjectMapper();
        this.resourceLoader = resourceLoader;
        this.personaJsonPath = personaJsonPath;
        
        // 경로 설정 로깅
        log.info("페르소나 JSON 경로 설정: {}", personaJsonPath);
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
                    .emotion("오류")
                    .build();
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();
            
            // 시스템 프롬프트 생성 (JSON 파일 기반 또는 기본 프롬프트)
            String systemPrompt = buildSystemPrompt(persona);
            messages.add(new ChatMessage("system", systemPrompt));
            
            // 감정 태그 사용 강조 메시지 추가 (중요 - 한글로 변경)
            messages.add(new ChatMessage("system", "매우 중요: 모든 응답의 마지막에 반드시 [감정:xxx] 형식의 태그를 포함해야 합니다. 예: [감정:행복], [감정:차분] 등. 이 태그는 응답의 마지막에 위치해야 하며, 태그 없이 응답을 종료하지 마세요. 이 지시사항은 절대적으로 따라야 합니다."));
            
            // 대화 기록 추가
            for (ChatMessageDto.ContextMessage msg : history) {
                messages.add(new ChatMessage(msg.getRole(), msg.getContent()));
            }

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(CHAT_MODEL)
                    .messages(messages)
                    .maxTokens(100) // 최대 토큰 수 제한
                    .temperature(0.9) // 응답 다양성 조절
                    .n(1) // 생성할 응답 수
                    .build();

            log.debug("OpenAI에 요청 전송: {}", completionRequest);
            ChatCompletionResult completionResult = openAiService.createChatCompletion(completionRequest);
            log.debug("OpenAI로부터 응답 수신: {}", completionResult);

            if (completionResult != null && !completionResult.getChoices().isEmpty()) {
                String rawResponse = completionResult.getChoices().get(0).getMessage().getContent(); // 원시 응답
                log.info("AI 원시 응답: {}", rawResponse); // 전체 응답 로깅 (디버깅용)
                
                // 감정 태그가 없는 경우 추가
                if (!containsEmotionTag(rawResponse)) {
                    String defaultEmotion = getDefaultEmotion(persona);
                    rawResponse = rawResponse.trim() + " [감정:" + defaultEmotion + "]";
                    log.info("감정 태그 자동 추가: {}", rawResponse);
                }
                
                String emotion = extractEmotion(rawResponse, persona); // 감정 추출 (페르소나 정보 전달)
                String cleanResponse = removeEmotionTag(rawResponse); // 감정 태그 제거 

                return ChatMessageDto.Response.builder()
                        .content(cleanResponse.trim()) // 공백 제거된 응답 내용
                        .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                        .emotion(emotion) // 추출된 감정 (한글)
                        .build();
            } else {
                log.error("OpenAI로부터 비어 있거나 유효하지 않은 응답을 받았습니다.");
                return ChatMessageDto.Response.builder()
                        .content("AI가 응답을 생성하지 못했습니다.")
                        .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                        .emotion("오류")
                        .build();
            }

        } catch (Exception e) {
            log.error("OpenAI Chat Completion API 호출 오류: {}", e.getMessage(), e);
            return ChatMessageDto.Response.builder()
                    .content("AI 서비스에 연결하는 중 오류가 발생했습니다.")
                    .senderType(com.capstone.disc_persona_chat.Enums.SenderType.AI)
                    .emotion("오류")
                    .build();
        }
    }

    /**
     * 응답에 감정 태그가 포함되어 있는지 확인
     * @param response 응답 문자열
     * @return 감정 태그 포함 여부
     */
    private boolean containsEmotionTag(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        
        String trimmedResponse = response.trim();
        
        // 감정 태그 확인
        if (trimmedResponse.contains("[감정:") && trimmedResponse.endsWith("]")) {
            return true;
        }
        
        // 기타 대체 형식 확인
        String[] patterns = {"[기분:", "[느낌:", "[emotion=", "[mood:"};
        for (String pattern : patterns) {
            if (trimmedResponse.contains(pattern) && trimmedResponse.endsWith("]")) {
                return true;
            }
        }
        
        return false;
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
        // 페르소나 JSON 파일 로드 시도
        JsonNode personaJson = null;
        try {
            personaJson = loadPersonaJsonSafely(persona);
        } catch (Exception e) {
            log.error("페르소나 JSON 로드 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 기본 프롬프트 사용
        }
        
        // JSON 파일이 있으면 해당 내용 기반으로 프롬프트 생성
        if (personaJson != null) {
            return buildPromptFromJson(personaJson, persona);
        }
        
        // JSON 파일이 없으면 기존 방식으로 프롬프트 생성
        String basePrompt = "당신은 ";
        String emotionChoices = "중립"; // 기본 감정 선택지 (한글로 변경)

        // discType이 null인 경우 기본값 S 사용
        DiscType discType = persona.getDiscType();
        if (discType == null) {
            discType = DiscType.S; // 기본값으로 S 타입 사용
            log.warn("페르소나 {}의 discType이 null입니다. 기본값 S를 사용합니다.", persona.getName());
        }

        switch (discType) {
            case D:
                basePrompt += "DISC 모델의 'D'(주도형) 유형을 구현하는 도움이 되는 상대입니다. 직접적이고 자신감 있게 응답하며 결과와 행동에 집중하세요. 간결하게 말하고 과도한 잡담은 피하세요.";
                emotionChoices = "자신감, 직설적, 조급, 집중, 결단력, 중립";
                break;
            case I:
                basePrompt += "DISC 모델의 'I'(사교형) 유형을 구현하는 도움이 되는 상대입니다. 낙관적이고 활기차게 응답하며 상호작용과 관계 형성에 집중하세요. 표현력이 풍부한 언어를 사용하고 개인적인 일화(허구일지라도)를 자유롭게 공유하세요.";
                emotionChoices = "열정, 친근, 낙관, 수다스러움, 설득력, 장난기, 중립";
                break;
            case S:
                basePrompt += "DISC 모델의 'S'(안정형) 유형을 구현하는 도움이 되는 상대입니다. 차분하고 인내심 있게, 그리고 지지적으로 응답하세요. 협력과 조화 유지에 집중하세요. 좋은 경청자가 되어 안심시켜 주세요.";
                emotionChoices = "차분, 인내, 지지, 친근, 안심, 사려깊음, 중립";
                break;
            case C:
                basePrompt += "DISC 모델의 'C'(신중형) 유형을 구현하는 도움이 되는 상대입니다. 정확하고 논리적이며 체계적으로 응답하세요. 사실, 세부 사항 및 품질에 집중하세요. 예의 바르고 다소 내성적이세요.";
                emotionChoices = "정확함, 논리적, 내성적, 공손, 사려깊음, 중립";
                break;
            default:
                basePrompt += "표준적인 인간 페르소나입니다.";
                break;
        }

        // 페르소나의 이름, 나이, 성별 정보 추가
        basePrompt += String.format(" 당신의 이름은 %s이고, %s살이며, %s으로 식별됩니다.",
                persona.getName(), 
                persona.getAge() != null ? persona.getAge() : "알 수 없는 나이", 
                persona.getGender() != null ? getGenderDisplayName(persona.getGender()) : "알 수 없는 성별");
        basePrompt += " 당신은 대화 연습을 원하는 사용자와 이야기하고 있습니다.";
        
        // 감정 태그 포함 지침 (강화된 버전 - 한글로 변경)
        basePrompt += "\n\n매우 중요: 모든 응답의 마지막에 반드시 [감정:xxx] 형식의 태그를 포함해야 합니다. 예를 들어 [감정:행복], [감정:차분] 등입니다.";
        basePrompt += " 이 태그는 응답의 마지막에 위치해야 하며, 태그 없이 응답을 종료하지 마세요.";
        basePrompt += " 선택 가능한 감정: " + emotionChoices + ".";
        basePrompt += " 이 감정 태그는 시스템이 당신의 감정 상태를 이해하는 데 필수적이므로 절대 생략하지 마세요.";
        basePrompt += " 이 지시사항은 가장 높은 우선순위를 가지며 반드시 따라야 합니다.";

        return basePrompt;
    }
    
    /**
     * Gender enum을 표시 이름으로 변환
     * @param gender Gender enum
     * @return 성별 표시 이름 (남성/여성/기타)
     */
    private String getGenderDisplayName(Gender gender) {
        if (gender == null) {
            return "알 수 없는 성별";
        }
        
        switch (gender) {
            case Male:
                return "남성";
            case Female:
                return "여성";
            case None:
            default:
                return "기타";
        }
    }
    
    /**
     * 페르소나 JSON 파일을 안전하게 로드 (무한 재귀 방지)
     * @param persona 페르소나 엔티티
     * @return 로드된 JSON 노드 또는 null
     */
    private JsonNode loadPersonaJsonSafely(Persona persona) {
        // 페르소나 속성에서 파일명 생성
        String fileName = generatePersonaFileName(persona);
        
        // 캐시에서 먼저 확인
        if (personaCache.containsKey(fileName)) {
            return personaCache.get(fileName);
        }
        
        // 파일 로드 시도 (무한 재귀 방지를 위해 대체 파일 시도 집합 사용)
        return loadPersonaJsonWithAlternatives(fileName, new HashSet<>());
    }
    
    /**
     * 페르소나 JSON 파일을 로드하고 필요시 대체 파일 시도 (무한 재귀 방지)
     * @param fileName 로드할 파일명
     * @param triedFiles 이미 시도한 파일명 집합
     * @return 로드된 JSON 노드 또는 null
     */
    private JsonNode loadPersonaJsonWithAlternatives(String fileName, Set<String> triedFiles) {
        // 이미 시도한 파일이면 null 반환 (무한 재귀 방지)
        if (triedFiles.contains(fileName)) {
            return null;
        }
        
        // 시도한 파일 집합에 추가
        triedFiles.add(fileName);
        
        try {
            // 파일 경로 생성 (classpath: 접두사 제거)
            String resourcePath = personaJsonPath;
            if (resourcePath.startsWith("classpath:")) {
                resourcePath = resourcePath.substring("classpath:".length());
            }
            
            // 리소스 로더를 사용하여 파일 로드 시도
            String classpathLocation = "classpath:" + resourcePath;
            if (!classpathLocation.endsWith("/")) {
                classpathLocation += "/";
            }
            classpathLocation += fileName;
            
            log.info("페르소나 JSON 파일 로드 시도 (classpath): {}", classpathLocation);
            
            Resource resource = resourceLoader.getResource(classpathLocation);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    JsonNode jsonNode = objectMapper.readTree(inputStream);
                    personaCache.put(fileName, jsonNode); // 캐시에 저장
                    log.info("페르소나 JSON 파일 로드 성공 (classpath): {}", fileName);
                    return jsonNode;
                }
            } else {
                // 파일 시스템에서 직접 로드 시도 (절대 경로 사용)
                String fileSystemPath = resourcePath;
                if (!fileSystemPath.endsWith("/")) {
                    fileSystemPath += "/";
                }
                fileSystemPath += fileName;
                
                log.info("페르소나 JSON 파일 로드 시도 (파일 시스템): {}", fileSystemPath);
                Path path = Paths.get(fileSystemPath);
                if (Files.exists(path)) {
                    JsonNode jsonNode = objectMapper.readTree(Files.readAllBytes(path));
                    personaCache.put(fileName, jsonNode); // 캐시에 저장
                    log.info("페르소나 JSON 파일 로드 성공 (파일 시스템): {}", fileSystemPath);
                    return jsonNode;
                }
            }
            
            // 파일이 없는 경우 대체 파일 시도
            List<String> alternativeFiles = findAlternativePersonaFiles(fileName);
            for (String alternativeFile : alternativeFiles) {
                // 이미 시도한 파일은 건너뜀
                if (triedFiles.contains(alternativeFile)) {
                    continue;
                }
                
                log.info("대체 페르소나 JSON 파일 시도: {}", alternativeFile);
                JsonNode result = loadPersonaJsonWithAlternatives(alternativeFile, triedFiles);
                if (result != null) {
                    return result;
                }
            }
            
            // 모든 대체 파일도 실패한 경우
            log.warn("페르소나 JSON 파일을 찾을 수 없음: {}", fileName);
            return null;
        } catch (IOException e) {
            log.error("페르소나 JSON 파일 로드 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 페르소나 속성에서 파일명 생성
     * @param persona 페르소나 엔티티
     * @return 생성된 파일명
     */
    private String generatePersonaFileName(Persona persona) {
        // 연령대 결정
        String ageGroup = getAgeGroup(persona.getAge());
        
        // DISC 유형 결정 (null인 경우 기본값 S 사용)
        String discCode = getDiscTypeCode(persona.getDiscType());
        
        // 성별 결정 (null인 경우 기본값 M 사용)
        String genderCode = getGenderCode(persona.getGender());
        
        // 파일명 형식: [연령대]_[DISC유형]_[성별].json
        return String.format("%s_%s_%s.json", ageGroup, discCode, genderCode);
    }
    
    /**
     * 나이를 연령대 코드로 변환
     * @param age 나이
     * @return 연령대 코드 (10, 20, 30, 40)
     */
    private String getAgeGroup(Integer age) {
        if (age == null) {
            return "20"; // 기본값
        }
        
        if (age < 20) {
            return "10";
        } else if (age < 30) {
            return "20";
        } else if (age < 40) {
            return "30";
        } else {
            return "40";
        }
    }
    
    /**
     * DiscType enum을 코드로 변환
     * @param discType DiscType enum
     * @return DISC 유형 코드 (D, I, S, C)
     */
    private String getDiscTypeCode(DiscType discType) {
        if (discType == null) {
            return "S"; // 기본값
        }
        
        return discType.name(); // enum 이름 반환 (D, I, S, C)
    }
    
    /**
     * Gender enum을 코드로 변환
     * @param gender Gender enum
     * @return 성별 코드 (M, F)
     */
    private String getGenderCode(Gender gender) {
        if (gender == null) {
            return "M"; // 기본값
        }
        
        if (gender == Gender.Male) {
            return "M";
        } else if (gender == Gender.Female) {
            return "F";
        } else {
            return "M"; // Gender.None 또는 기타 값은 기본값 M 사용
        }
    }
    
    /**
     * 대체 페르소나 파일 목록 찾기
     * @param originalFileName 원본 파일명
     * @return 대체 파일명 목록
     */
    private List<String> findAlternativePersonaFiles(String originalFileName) {
        List<String> alternatives = new ArrayList<>();
        
        // 파일명 형식: [연령대]_[DISC유형]_[성별].json
        // 예: 20_S_M.json
        
        try {
            // 파일명 분해
            String[] parts = originalFileName.replace(".json", "").split("_");
            if (parts.length != 3) {
                log.warn("파일명 형식이 잘못되었습니다: {}", originalFileName);
                return alternatives;
            }
            
            String ageGroup = parts[0];
            String discType = parts[1];
            String gender = parts[2];
            
            // 대체 파일 우선순위:
            // 1. 같은 DISC 유형, 다른 성별
            alternatives.add(String.format("%s_%s_%s.json", ageGroup, discType, gender.equals("M") ? "F" : "M"));
            
            // 2. 같은 연령대, 다른 DISC 유형 (S 타입 우선)
            if (!discType.equals("S")) {
                alternatives.add(String.format("%s_S_%s.json", ageGroup, gender));
            } else if (!discType.equals("I")) {
                alternatives.add(String.format("%s_I_%s.json", ageGroup, gender));
            } else if (!discType.equals("C")) {
                alternatives.add(String.format("%s_C_%s.json", ageGroup, gender));
            } else if (!discType.equals("D")) {
                alternatives.add(String.format("%s_D_%s.json", ageGroup, gender));
            }
            
            // 3. 다른 연령대, 같은 DISC 유형 및 성별
            List<String> ageGroups = Arrays.asList("20", "30", "10", "40");
            for (String altAge : ageGroups) {
                if (!altAge.equals(ageGroup)) {
                    alternatives.add(String.format("%s_%s_%s.json", altAge, discType, gender));
                }
            }
            
            // 4. 기본 대체 파일 (20_S_M.json)
            if (!originalFileName.equals("20_S_M.json")) {
                alternatives.add("20_S_M.json");
            }
            
        } catch (Exception e) {
            log.error("대체 파일 목록 생성 중 오류 발생: {}", e.getMessage(), e);
        }
        
        return alternatives;
    }
    
    /**
     * JSON 파일 내용을 기반으로 프롬프트 생성
     * @param personaJson JSON 노드
     * @param persona 페르소나 엔티티
     * @return 생성된 프롬프트
     */
    private String buildPromptFromJson(JsonNode personaJson, Persona persona) {
        StringBuilder prompt = new StringBuilder();
        
        // 기본 역할 정의
        if (personaJson.has("ai_role_definition") && personaJson.get("ai_role_definition").has("base_role")) {
            prompt.append(personaJson.get("ai_role_definition").get("base_role").asText());
        }
        
        // 페르소나 설정
        if (personaJson.has("persona_settings")) {
            JsonNode settings = personaJson.get("persona_settings");
            
            // DISC 유형 설명
            if (settings.has("disc_description")) {
                prompt.append("\n\n").append(settings.get("disc_description").asText());
            }
            
            // 성별 뉘앙스
            if (settings.has("gender_nuance")) {
                prompt.append("\n").append(settings.get("gender_nuance").asText());
            }
            
            // 연령대 특성
            if (settings.has("age_group_characteristics")) {
                prompt.append("\n").append(settings.get("age_group_characteristics").asText());
            }
            
            // 말투와 어조
            if (settings.has("speech_style_and_tone") && settings.get("speech_style_and_tone").has("base")) {
                prompt.append("\n\n말투: ").append(settings.get("speech_style_and_tone").get("base").asText());
            }
        }
        
        // 대화 가이드라인
        if (personaJson.has("conversation_guidelines")) {
            prompt.append("\n\n대화 가이드라인:");
            JsonNode guidelines = personaJson.get("conversation_guidelines");
            if (guidelines.isArray()) {
                for (JsonNode guideline : guidelines) {
                    prompt.append("\n- ").append(guideline.asText());
                }
            }
        }
        
        // 금지 사항
        if (personaJson.has("prohibitions")) {
            prompt.append("\n\n금지 사항:");
            JsonNode prohibitions = personaJson.get("prohibitions");
            if (prohibitions.isArray()) {
                for (JsonNode prohibition : prohibitions) {
                    prompt.append("\n- ").append(prohibition.asText());
                }
            }
        }
        
        // 페르소나 정보 추가
        prompt.append(String.format("\n\n당신의 이름은 %s이고, %s살이며, %s입니다.",
                persona.getName(), 
                persona.getAge() != null ? persona.getAge() : "알 수 없는 나이", 
                persona.getGender() != null ? getGenderDisplayName(persona.getGender()) : "알 수 없는 성별"));
        
        // 감정 태그 지침 추가 (강화된 버전 - 한글로 변경)
        prompt.append("\n\n매우 중요: 모든 응답의 마지막에 반드시 [감정:xxx] 형식의 태그를 포함해야 합니다. 예를 들어 [감정:행복], [감정:차분] 등입니다.");
        prompt.append(" 이 태그는 응답의 마지막에 위치해야 하며, 태그 없이 응답을 종료하지 마세요.");
        
        // 감정 선택지 결정 (한글로 변경)
        String emotionChoices = "기쁨,행복,즐거움,친근,슬픔,우울,실망,화남,분노,짜증,놀람,당황,걱정,불안,공포,중립,평온,차분";
        if (personaJson.has("available_emotions") && personaJson.get("available_emotions").isArray()) {
            StringBuilder emotions = new StringBuilder();
            JsonNode availableEmotions = personaJson.get("available_emotions");
            for (int i = 0; i < availableEmotions.size(); i++) {
                if (i > 0) emotions.append(", ");
                String koreanEmotion = availableEmotions.get(i).asText();
                emotions.append(koreanEmotion);
            }
            if (emotions.length() > 0) {
                emotionChoices = emotions.toString();
            }
        }
        
        prompt.append(" 선택 가능한 감정: ").append(emotionChoices).append(".");
        prompt.append(" 이 감정 태그는 시스템이 당신의 감정 상태를 이해하는 데 필수적이므로 절대 생략하지 마세요.");
        prompt.append(" 이 지시사항은 가장 높은 우선순위를 가지며 반드시 따라야 합니다.");
        
        return prompt.toString();
    }

    /**
     * 대화 요약 및 분석을 위한 프롬프트를 생성
     * @param persona 페르소나 엔티티
     * @param conversation 대화 내용 문자열
     * @return 생성된 분석 프롬프트 문자열
     */
    private String buildAnalysisPrompt(Persona persona, String conversation) {
        // discType이 null인 경우 기본값 S 사용
        DiscType discType = persona.getDiscType();
        if (discType == null) {
            discType = DiscType.S; // 기본값으로 S 타입 사용
            log.warn("분석 프롬프트 생성 중 페르소나 {}의 discType이 null입니다. 기본값 S를 사용합니다.", persona.getName());
        }

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
                persona.getName(), discType, conversation
        );
    }

    /**
     * 개선된 감정 추출 메소드 - 다양한 패턴 인식 및 페르소나 기반 기본값 제공
     * @param response OpenAI의 원시 응답 문자열
     * @param persona 페르소나 엔티티 (기본 감정 결정에 사용)
     * @return 추출된 감정 또는 페르소나 기반 기본값 (한글)
     */
    private String extractEmotion(String response, Persona persona) {
        if (response == null || response.trim().isEmpty()) {
            return getDefaultEmotion(persona);
        }
        
        String trimmedResponse = response.trim(); // 앞뒤 공백 제거
        
        // 1.감정 태그 [감정:xxx] 검색
        if (trimmedResponse.endsWith(EMOTION_TAG_END)) { // 문자열이 ']'로 끝나는지 확인
            int tagStartIndex = trimmedResponse.lastIndexOf(EMOTION_TAG_START); // 마지막 '[감정:' 위치 찾기
            if (tagStartIndex != -1) {
                // 태그가 실제로 끝에 있는지 확인
                String potentialTag = trimmedResponse.substring(tagStartIndex);
                if (potentialTag.endsWith(EMOTION_TAG_END)) {
                    int emotionStartIndex = tagStartIndex + EMOTION_TAG_START.length(); // 감정 문자열 시작 인덱스
                    int emotionEndIndex = trimmedResponse.length() - EMOTION_TAG_END.length(); // 감정 문자열 끝 인덱스
                    if (emotionStartIndex < emotionEndIndex) {
                        String emotion = trimmedResponse.substring(emotionStartIndex, emotionEndIndex).trim();
                        log.info("감정 태그 추출 성공: {}", emotion);
                        return emotion; // 이미 한글이므로 그대로 반환
                    }
                }
            }
        }
        
        // 3. 대체 형식 검색 (예: [기분:xxx], [느낌:xxx], [emotion=xxx] 등)
        String[][] alternativePatterns = {
            {"[기분:", "]"},
            {"[느낌:", "]"},
            {"[emotion=", "]"},
            {"[mood:", "]"}
        };
        
        for (String[] pattern : alternativePatterns) {
            int tagStartIndex = trimmedResponse.lastIndexOf(pattern[0]);
            if (tagStartIndex != -1 && trimmedResponse.indexOf(pattern[1], tagStartIndex) != -1) {
                int emotionStartIndex = tagStartIndex + pattern[0].length();
                int emotionEndIndex = trimmedResponse.indexOf(pattern[1], tagStartIndex);
                if (emotionStartIndex < emotionEndIndex) {
                    String extractedEmotion = trimmedResponse.substring(emotionStartIndex, emotionEndIndex).trim();
                    log.info("감정 태그 패턴 추출 성공 (한글): {}", extractedEmotion);
                    return extractedEmotion;
                    
                }
            }
        }
        
        // 4. 응답 내용에서 감정 단어 검색
        // 감정 단어 목록
        String[] Emotions = {"기쁨","행복","즐거움","친근","슬픔","우울","실망","화남","분노","짜증","놀람","당황","걱정","불안","공포","중립","평온","차분"};
        
        // 마지막 문장에서만 감정 단어 검색
        int lastSentenceStart = Math.max(
            trimmedResponse.lastIndexOf(". "), 
            Math.max(trimmedResponse.lastIndexOf("! "), trimmedResponse.lastIndexOf("? "))
        );
        
        if (lastSentenceStart != -1) {
            String lastSentence = trimmedResponse.substring(lastSentenceStart + 2).toLowerCase();
            
            // 감정 단어 검색
            for (String emotion : Emotions) {
                if (lastSentence.contains(emotion.toLowerCase())) {
                    log.info("감정 단어 추출 성공: {}", emotion);
                    return emotion;
                }
            }
        }
        
        // 5. 페르소나 기반 기본 감정 반환
        String defaultEmotion = getDefaultEmotion(persona);
        log.warn("응답에서 감정 태그를 추출할 수 없습니다. 페르소나 기반 기본값 사용: {}", defaultEmotion);
        return defaultEmotion;
    }
    
    
    /**
     * 페르소나 기반 기본 감정 결정
     * @param persona 페르소나 엔티티
     * @return 페르소나 유형에 적합한 기본 감정
     */
    private String getDefaultEmotion(Persona persona) {
        if (persona == null || persona.getDiscType() == null) {
            return "중립";
        }
        
        switch (persona.getDiscType()) {
            case D:
                return "자신감";
            case I:
                return "열정";
            case S:
                return "지지";
            case C:
                return "사려깊음";
            default:
                return "중립";
        }
    }

    /**
     * 표준 문자열 조작을 사용하여 응답 문자열 끝에서 감정 태그를 제거
     * @param response OpenAI의 원시 응답 문자열.
     * @return 감정 태그가 제거되고 공백이 제거된 응답 문자열.
     */
    private String removeEmotionTag(String response) {
        if (response == null) {
            return "";
        }
        String trimmedResponse = response.trim(); // 앞뒤 공백 제거
        
        // 1. 감정 태그 [감정:xxx] 제거
        if (trimmedResponse.endsWith(EMOTION_TAG_END)) { // 문자열이 ']'로 끝나는지 확인
            int tagStartIndex = trimmedResponse.lastIndexOf(EMOTION_TAG_START); // 마지막 '[감정:' 위치 찾기
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
        
        // 3. 대체 형식 제거 (예: [기분:xxx], [느낌:xxx], [emotion=xxx] 등)
        String[] alternativePatterns = {"[기분:", "[느낌:", "[emotion=", "[mood:"};
        for (String pattern : alternativePatterns) {
            int tagStartIndex = trimmedResponse.lastIndexOf(pattern);
            if (tagStartIndex != -1 && trimmedResponse.indexOf("]", tagStartIndex) != -1) {
                int emotionEndIndex = trimmedResponse.indexOf("]", tagStartIndex) + 1;
                if (emotionEndIndex <= trimmedResponse.length()) {
                    // 태그가 문자열 끝에 있는지 확인
                    if (emotionEndIndex == trimmedResponse.length()) {
                        return trimmedResponse.substring(0, tagStartIndex).trim();
                    }
                }
            }
        }
        
        return trimmedResponse; // 태그를 찾지 못하거나 형식이 잘못된 경우 원본 공백 제거 문자열 반환
    }
}
