package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.apiPayload.code.status.ErrorStatus;
import com.capstone.disc_persona_chat.apiPayload.exception.UserHandler;
import com.capstone.disc_persona_chat.config.security.jwt.JwtTokenProvider;
import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.exception.UserNotFoundException;
import com.capstone.disc_persona_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO.UserInfoDTO getUserInfo(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // 사용자 정보를 DTO로 변환
        return UserResponseDTO.UserInfoDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .role(user.getRole())
                .socialType(user.getSocialType())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    @Override
    @Transactional
    public UserResponseDTO.UserInfoDTO updateUserInfo(Long userId, UserRequestDTO.UpdateUserDto request) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    
        // 변경할 값 준비
        String newName = request.getName() != null ? request.getName() : user.getName();
        String newPassword = request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : user.getPassword();
        Gender newGender = user.getGender();
    
        // Integer를 Gender enum으로 변환
        if (request.getGender() != null) {
            switch(request.getGender()) {
                case 0:
                    newGender = Gender.Male;
                    break;
                case 1:
                    newGender = Gender.Female;
                    break;
                default:
                    newGender = Gender.None;
                    break;
            }
        }
    
        // 새 Users 객체 생성 및 저장
        Users updatedUser = Users.builder()
                .id(user.getId())
                .name(newName)
                .email(user.getEmail())
                .password(newPassword)
                .gender(newGender)
                .role(user.getRole())
                .socialType(user.getSocialType())
                .build();
        
        updatedUser = userRepository.save(updatedUser);
        
        // DTO로 변환하여 반환
        return UserResponseDTO.UserInfoDTO.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .gender(updatedUser.getGender())
                .role(updatedUser.getRole())
                .socialType(updatedUser.getSocialType())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        // 이메일이 존재하지 않으면 true(사용 가능), 존재하면 false(사용 불가) 반환
        return !userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        // 닉네임이 존재하지 않으면 true(사용 가능), 존재하면 false(사용 불가) 반환
        return !userRepository.existsByName(nickname);
    }
}
