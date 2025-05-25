package com.capstone.disc_persona_chat.dto;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResultDTO {
        Long userId;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResultDTO {
        Long userId;
        String accessToken;
    }
    
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long id;
        private String name;
        private String email;
        private Gender gender;
        private Role role;
        private String socialType;
        private LocalDateTime createdAt;
    }
}
