package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;

public interface UserService {
    Users joinUser(UserRequestDTO.JoinDto request);
    UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request);
    
    /**
     * 현재 로그인한 사용자의 정보를 조회
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자 정보 DTO
     */
    UserResponseDTO.UserInfoDTO getUserInfo(Long userId);
    
    /**
     * 현재 로그인한 사용자의 정보를 수정
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param request 수정할 정보가 담긴 요청 DTO
     * @return 수정된 사용자 정보 DTO
     */
    UserResponseDTO.UserInfoDTO updateUserInfo(Long userId, UserRequestDTO.UpdateUserDto request);
    
    /**
     * 이메일 중복 체크
     *
     * @param email 중복 체크할 이메일
     * @return 사용 가능한 이메일이면 true, 이미 사용 중이면 false
     */
    boolean isEmailAvailable(String email);
    
    /**
     * 닉네임 중복 체크
     *
     * @param nickname 중복 체크할 닉네임
     * @return 사용 가능한 닉네임이면 true, 이미 사용 중이면 false
     */
    boolean isNicknameAvailable(String nickname);
}
