package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.apiPayload.code.status.ErrorStatus;
import com.capstone.disc_persona_chat.apiPayload.exception.UserHandler;
import com.capstone.disc_persona_chat.config.security.jwt.JwtTokenProvider;
import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserConverter userConverter;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserConverter userConverter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userConverter = userConverter;
    }

    @Override
    public Users joinUser(UserRequestDTO.JoinDto request) {
        // 비밀번호 암호화
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 통합 컨버터를 사용하여 DTO를 엔티티로 변환
        Users newUser = userConverter.toEntity(request);
        return userRepository.save(newUser);
    }

    @Override
    public UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserHandler(ErrorStatus.INVALID_PASSWORD);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                Collections.singleton(() -> user.getRole().name())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 통합 컨버터를 사용하여 엔티티와 토큰으로 DTO 생성
        return userConverter.toLoginResultDto(user, accessToken);
    }
}
