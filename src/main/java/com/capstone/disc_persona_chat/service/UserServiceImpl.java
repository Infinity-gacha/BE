package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.apiPayload.code.status.ErrorStatus;
import com.capstone.disc_persona_chat.apiPayload.exception.UserHandler;
import com.capstone.disc_persona_chat.config.security.jwt.JwtTokenProvider;
import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Users joinUser(UserRequestDTO.JoinDto request){
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        Users newUser = UserConverter.toUser(request);
        return userRepository.save(newUser);
    }

    @Override
    public UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request) {
        // 원래 그냥 user type
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserHandler(ErrorStatus.INVALID_PASSWORD);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                Collections.singleton(() -> user.getRole().name())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        return UserConverter.toLoginResultDTO(
                user.getId(),
                accessToken
        );
    }
}
