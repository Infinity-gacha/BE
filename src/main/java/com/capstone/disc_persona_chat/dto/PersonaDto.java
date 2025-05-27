package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.DiscType;
import com.capstone.disc_persona_chat.Enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PersonaDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private DiscType discType; // 페르소나의 DISC 유형
        private String name;       // 페르소나의 이름
        private Integer age;       // 페르소나의 나이 (선택 사항)
        private Gender gender;     // 페르소나의 성별 (선택 사항)
  
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;           // 페르소나의 고유 ID
        private DiscType discType; // 페르소나의 DISC 유형
        private String name;       // 페르소나의 이름
        private Integer age;       // 페르소나의 나이
        private Gender gender;     // 페르소나의 성별

    }
}

