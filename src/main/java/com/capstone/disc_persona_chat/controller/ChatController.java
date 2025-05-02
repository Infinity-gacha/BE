package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.dto.ChatMessageDto;
import com.capstone.disc_persona_chat.dto.ChatSummaryDto;
import com.capstone.disc_persona_chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas/{personaId}") // 페르소나별 작업의 기본 경로
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/personas/{personaId}/chat : 사용자 메시지를 보내고 AI 응답을 받기기
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatMessageDto.Response> sendMessage(
            @PathVariable Long personaId,
            @RequestBody ChatMessageDto.Request request) {
        // PersonaNotFoundException
        ChatMessageDto.Response aiResponse = chatService.processMessage(personaId, request);
        return ResponseEntity.ok(aiResponse);
    }

    /**
     * GET /api/personas/{personaId}/chat : 페르소나의 채팅 기록을 검색
     */
    @GetMapping("/chat")
    public ResponseEntity<List<ChatMessageDto.Response>> getChatHistory(@PathVariable Long personaId) {
        List<ChatMessageDto.Response> history = chatService.getChatHistory(personaId);
        return ResponseEntity.ok(history);
    }

    /**
     * POST /api/personas/{personaId}/summary : 채팅 요약 생성을 트리거
     */
    @PostMapping("/summary")
    public ResponseEntity<ChatSummaryDto.Response> generateSummary(@PathVariable Long personaId) {
        ChatSummaryDto.Response summary = chatService.generateAndSaveSummary(personaId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            // 요약 생성 실패 또는 기록 없음 케이스 처리
            return ResponseEntity.noContent().build(); 
        }
    }

    /**
     * GET /api/personas/{personaId}/summary : 페르소나의 최신 채팅 요약을 검색
     */
    @GetMapping("/summary")
    public ResponseEntity<ChatSummaryDto.Response> getLatestSummary(@PathVariable Long personaId) {
        ChatSummaryDto.Response summary = chatService.getLatestChatSummary(personaId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.notFound().build(); // 요약 없으면 404 반환
        }
    }

    /**
     * GET /api/personas/{personaId}/summary/all : 페르소나의 모든 채팅 요약을 검색
     */
    @GetMapping("/summary/all")
    public ResponseEntity<List<ChatSummaryDto.Response>> getAllSummaries(@PathVariable Long personaId) {
        List<ChatSummaryDto.Response> summaries = chatService.getAllChatSummaries(personaId);
        return ResponseEntity.ok(summaries);
    }

}

