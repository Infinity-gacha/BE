package com.capstone.disc_persona_chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// EmotionController.java
@RestController
@RequestMapping("/api")
public class EmotionController {

    @PostMapping("/emotion")
    public ResponseEntity<String> receiveEmotion(@RequestBody Map<String, String> payload) {
        String emotion = payload.get("emotion");
        System.out.println("받은 감정: " + emotion);

        // 나중에 DB 저장 or 프론트에 전달 가능
        return ResponseEntity.ok("감정 수신 완료: " + emotion);
    }
}
