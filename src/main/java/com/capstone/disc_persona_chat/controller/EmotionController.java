package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.dto.EmotionDto;
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
    public ResponseEntity<EmotionDto.Response> receiveEmotion(@RequestBody Map<String, String> payload) {
        String emotion = payload.get("emotion");
        System.out.println("받은 감정: " + emotion);

        EmotionDto.Response emotionResponse = EmotionDto.Response.builder()
                .emotion(emotion)
                .build();

        return ResponseEntity.ok(emotionResponse);
    }

}
