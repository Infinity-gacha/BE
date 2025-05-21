package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;

public interface UserService {
    public Users joinUser(UserRequestDTO.JoinDto request);
    public UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request);
}
