package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.apiPayload.ApiResponse;
import com.capstone.disc_persona_chat.config.security.SecurityUtils;
import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private final UserService userService;
    private final UserConverter userConverter;

    @Autowired
    public UserRestController(UserService userService, UserConverter userConverter) {
        this.userService = userService;
        this.userConverter = userConverter;
    }

    @PostMapping("/join")
    public ApiResponse<UserResponseDTO.JoinResultDTO> join(@RequestBody @Valid UserRequestDTO.JoinDto request) {
        Users user = userService.joinUser(request);
        return ApiResponse.onSuccess(userConverter.toJoinResultDto(user));
    }

    @PostMapping("/login")
    @Operation(summary = "유저 로그인 API", description = "유저가 로그인하는 API입니다.")
    public ApiResponse<UserResponseDTO.LoginResultDTO> login(@RequestBody @Valid UserRequestDTO.LoginRequestDTO request) {
        return ApiResponse.onSuccess(userService.loginUser(request));
    }
    
    @GetMapping("/me")
    @Operation(summary = "현재 로그인한 사용자 정보 조회 API", description = "현재 로그인한 사용자의 정보를 조회하는 API입니다.")
    public ApiResponse<UserResponseDTO.UserInfoDTO> getCurrentUserInfo() {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 현재 사용자 정보 조회
        UserResponseDTO.UserInfoDTO userInfo = userService.getUserInfo(currentUserId);
        return ApiResponse.onSuccess(userInfo);
    }
    
    @PutMapping("/me")
    @Operation(summary = "현재 로그인한 사용자 정보 수정 API", description = "현재 로그인한 사용자의 정보를 수정하는 API입니다.")
    public ApiResponse<UserResponseDTO.UserInfoDTO> updateCurrentUserInfo(@RequestBody @Valid UserRequestDTO.UpdateUserDto request) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 현재 사용자 정보 수정
        UserResponseDTO.UserInfoDTO updatedUserInfo = userService.updateUserInfo(currentUserId, request);
        return ApiResponse.onSuccess(updatedUserInfo);
    }
}
