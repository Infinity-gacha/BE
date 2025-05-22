package com.capstone.disc_persona_chat.converter.impl;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * UserConverter 인터페이스의 구현 클래스
 * UsersConverter와 UserConverter의 기능을 통합
 */
@Component
public class UserConverterImpl implements UserConverter {

    /**
     * Users 엔티티를 UserResponseDTO.JoinResultDTO로 변환
     * @param entity Users 엔티티
     * @return 변환된 UserResponseDTO.JoinResultDTO
     */
    @Override
    public UserResponseDTO.JoinResultDTO toJoinResultDto(Users entity) {
        if (entity == null) {
            return null;
        }
        
        return UserResponseDTO.JoinResultDTO.builder()
                .userId(entity.getId())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now())
                .build();
    }
    
    /**
     * Users 엔티티와 액세스 토큰을 UserResponseDTO.LoginResultDTO로 변환
     * @param entity Users 엔티티
     * @param accessToken 액세스 토큰
     * @return 변환된 UserResponseDTO.LoginResultDTO
     */
    @Override
    public UserResponseDTO.LoginResultDTO toLoginResultDto(Users entity, String accessToken) {
        if (entity == null) {
            return null;
        }
        
        return UserResponseDTO.LoginResultDTO.builder()
                .userId(entity.getId())
                .accessToken(accessToken)
                .build();
    }
    
    /**
     * 사용자 ID와 액세스 토큰을 UserResponseDTO.LoginResultDTO로 변환
     * @param userId 사용자 ID
     * @param accessToken 액세스 토큰
     * @return 변환된 UserResponseDTO.LoginResultDTO
     */
    @Override
    public UserResponseDTO.LoginResultDTO toLoginResultDto(Long userId, String accessToken) {
        return UserResponseDTO.LoginResultDTO.builder()
                .userId(userId)
                .accessToken(accessToken)
                .build();
    }
    
    /**
     * UserRequestDTO.JoinDto를 Users 엔티티로 변환
     * @param dto UserRequestDTO.JoinDto
     * @return 변환된 Users 엔티티
     */
    @Override
    public Users toEntity(UserRequestDTO.JoinDto dto) {
        if (dto == null) {
            return null;
        }
        
        // Gender enum 변환 처리
        Gender gender = null;
        if (dto.getGender() != null) {
            switch (dto.getGender()) {
                case 0:
                case 1:
                    gender = Gender.Male;
                    break;
                case 2:
                    gender = Gender.Female;
                    break;
                default:
                    gender = Gender.None;
                    break;
            }
        }
        
        return Users.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .role(dto.getRole())
                .gender(gender)
                .socialType(dto.getSocial_type())
                .build();
    }
    
    /**
     * UserRequestDTO.LoginRequestDTO에서 필요한 정보를 추출하여 로그인 검증에 사용
     * @param dto UserRequestDTO.LoginRequestDTO
     * @return 이메일과 비밀번호 정보가 포함된 Users 엔티티
     */
    @Override
    public Users toLoginEntity(UserRequestDTO.LoginRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Users.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }
}
