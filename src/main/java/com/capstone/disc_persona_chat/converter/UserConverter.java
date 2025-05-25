package com.capstone.disc_persona_chat.converter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;

/**
 * Users 엔티티와 UserRequestDTO/UserResponseDTO 간의 변환을 위한 통합 컨버터 인터페이스
 * UsersConverter와 UserConverter의 기능을 통합
 */
public interface UserConverter {
    
    /**
     * Users 엔티티를 UserResponseDTO.JoinResultDTO로 변환
     * @param entity Users 엔티티
     * @return 변환된 UserResponseDTO.JoinResultDTO
     */
    UserResponseDTO.JoinResultDTO toJoinResultDto(Users entity);
    
    /**
     * Users 엔티티와 액세스 토큰을 UserResponseDTO.LoginResultDTO로 변환
     * @param entity Users 엔티티
     * @param accessToken 액세스 토큰
     * @return 변환된 UserResponseDTO.LoginResultDTO
     */
    UserResponseDTO.LoginResultDTO toLoginResultDto(Users entity, String accessToken);
    
    /**
     * 사용자 ID와 액세스 토큰을 UserResponseDTO.LoginResultDTO로 변환
     * @param userId 사용자 ID
     * @param accessToken 액세스 토큰
     * @return 변환된 UserResponseDTO.LoginResultDTO
     */
    UserResponseDTO.LoginResultDTO toLoginResultDto(Long userId, String accessToken);
    
    /**
     * UserRequestDTO.JoinDto를 Users 엔티티로 변환
     * @param dto UserRequestDTO.JoinDto
     * @return 변환된 Users 엔티티
     */
    Users toEntity(UserRequestDTO.JoinDto dto);
    
    /**
     * UserRequestDTO.LoginRequestDTO에서 필요한 정보를 추출하여 로그인 검증에 사용
     * @param dto UserRequestDTO.LoginRequestDTO
     * @return 이메일과 비밀번호 정보가 포함된 Users 엔티티
     */
    Users toLoginEntity(UserRequestDTO.LoginRequestDTO dto);
}
