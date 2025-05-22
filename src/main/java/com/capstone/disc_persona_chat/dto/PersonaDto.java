package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.DiscType;
import com.capstone.disc_persona_chat.domain.entity.Persona;
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
        private String gender;     // 페르소나의 성별 (선택 사항)

        // DTO를 엔티티로 변환하는 메소드
        public Persona toEntity() {
            return Persona.builder()
                    .discType(this.discType)
                    .name(this.name)
                    .age(this.age)
                    .gender(this.gender)
                    .build();
        }
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
        private String gender;     // 페르소나의 성별

        // 엔티티를 DTO로 변환하는 정적 팩토리 메소드
        public static Response fromEntity(Persona persona) {
            return Response.builder()
                    .id(persona.getId())
                    .discType(persona.getDiscType())
                    .name(persona.getName())
                    .age(persona.getAge())
                    .gender(persona.getGender())
                    .build();
        }
    }
}

