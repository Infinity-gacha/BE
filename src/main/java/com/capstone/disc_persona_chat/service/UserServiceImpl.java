package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.converter.UserConverter;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.UserRequestDTO;
import com.capstone.disc_persona_chat.dto.UserResponseDTO;
import com.capstone.disc_persona_chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Users joinUser(UserRequestDTO.JoinDto request){
        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // 인코딩된 비밀번호를 swt한 새로운 request 생성
        request.setPassword(encodedPassword);

        Users newUser = UserConverter.toUser(request);
        return userRepository.save(newUser);
    }
}
