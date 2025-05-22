package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.service.UserService;
import com.capstone.disc_persona_chat.apiPayload.ApiResponse;
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
}
