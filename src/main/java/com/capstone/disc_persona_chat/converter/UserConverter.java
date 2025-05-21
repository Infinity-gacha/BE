package com.capstone.disc_persona_chat.converter;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.Enums.Role;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;

import java.time.LocalDateTime;

public class UserConverter {

    public static UserResponseDTO.JoinResultDTO toJoinResultDTO(Users user) {
        return UserResponseDTO.JoinResultDTO.builder()
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Users toUser(UserRequestDTO.JoinDto request) {
        Gender gender = null;
        Role role = null;

        switch (request.getGender()) {
            case 1:
                gender = Gender.MALE;
                break;
            case 2:
                gender = Gender.FEMALE;
                break;
            case 3:
                gender = Gender.NONE;
        }

//        switch (request.getRole()) {
//            case 1:
//                role = Role.ADMIN;
//                break;
//            case 2:
//                role = Role.USER;
//                break;
//        }

        return Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .gender(gender)
                .role(request.getRole())
                .password(request.getPassword())
                .socialType(request.getSocial_type())
                .build();
    }
}
