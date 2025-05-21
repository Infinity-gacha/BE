package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;

public interface UserService {
    public Users joinUser(UserRequestDTO.JoinDto request);
}
