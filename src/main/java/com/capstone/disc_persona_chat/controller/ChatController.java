package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.config.security.SecurityUtils;
import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/personas/{personaId}") // 페르소나별 작업의 기본 경로
public class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/personas/{personaId}/chat : 사용자 메시지를 보내고 AI 응답을 받기
     * 현재 로그인한 사용자만 자신의 페르소나에 대해 메시지를 보낼 수 있음
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatMessageDto.Response> sendMessage(
            @PathVariable Long personaId,
            @RequestBody ChatMessageDto.Request request) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 사용자 ID와 페르소나 ID를 함께 전달하여 권한 검증
        ChatMessageDto.Response aiResponse = chatService.processMessageWithUserCheck(personaId, request, currentUserId);
        return ResponseEntity.ok(aiResponse);
    }

    /**
     * GET /api/personas/{personaId}/chat : 페르소나의 채팅 기록을 검색
     * 현재 로그인한 사용자만 자신의 페르소나에 대한 채팅 기록을 볼 수 있음
     */
    @GetMapping("/chat")
    public ResponseEntity<List<ChatMessageDto.Response>> getChatHistory(@PathVariable Long personaId) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 사용자 ID와 페르소나 ID를 함께 전달하여 권한 검증
        List<ChatMessageDto.Response> history = chatService.getChatHistoryWithUserCheck(personaId, currentUserId);
        return ResponseEntity.ok(history);
    }

    /**
     * POST /api/personas/{personaId}/summary : 채팅 요약 생성을 트리거
     * 현재 로그인한 사용자만 자신의 페르소나에 대한 요약을 생성할 수 있음
     */
    @PostMapping("/summary")
    public ResponseEntity<ChatSummaryDto.Response> generateSummary(@PathVariable Long personaId) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 사용자 ID와 페르소나 ID를 함께 전달하여 권한 검증
        ChatSummaryDto.Response summary = chatService.generateAndSaveSummaryWithUserCheck(personaId, currentUserId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            // 요약 생성 실패 또는 기록 없음 케이스 처리
            return ResponseEntity.noContent().build(); 
        }
    }

    /**
     * GET /api/personas/{personaId}/summary : 페르소나의 최신 채팅 요약을 검색
     * 현재 로그인한 사용자만 자신의 페르소나에 대한 요약을 볼 수 있음
     */
    @GetMapping("/summary")
    public ResponseEntity<ChatSummaryDto.Response> getLatestSummary(@PathVariable Long personaId) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 사용자 ID와 페르소나 ID를 함께 전달하여 권한 검증
        ChatSummaryDto.Response summary = chatService.getLatestChatSummaryWithUserCheck(personaId, currentUserId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.notFound().build(); // 요약 없으면 404 반환
        }
    }

    /**
     * GET /api/personas/{personaId}/summary/all : 페르소나의 모든 채팅 요약을 검색
     * 현재 로그인한 사용자만 자신의 페르소나에 대한 요약을 볼 수 있음
     */
    @GetMapping("/summary/all")
    public ResponseEntity<List<ChatSummaryDto.Response>> getAllSummaries(@PathVariable Long personaId) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 사용자 ID와 페르소나 ID를 함께 전달하여 권한 검증
        List<ChatSummaryDto.Response> summaries = chatService.getAllChatSummariesWithUserCheck(personaId, currentUserId);
        return ResponseEntity.ok(summaries);
    }
}
