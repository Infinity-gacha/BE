package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import com.capstone.disc_persona_chat.dto.PersonaDto;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.exception.PersonaNotFoundException;
import com.capstone.disc_persona_chat.exception.UnauthorizedAccessException;
import com.capstone.disc_persona_chat.exception.UserNotFoundException;
import com.capstone.disc_persona_chat.repository.ChatMessageRepository;
import com.capstone.disc_persona_chat.repository.ChatSummaryRepository;
import com.capstone.disc_persona_chat.repository.PersonaRepository;
import com.capstone.disc_persona_chat.repository.UserPersonaRepository;
import com.capstone.disc_persona_chat.repository.UserRepository;
import com.capstone.disc_persona_chat.converter.PersonaConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSummaryRepository chatSummaryRepository;
    private final PersonaConverter personaConverter;
    private final UserPersonaRepository userPersonaRepository;
    private final PersonaProfileImageService personaProfileImageService; 

    /**
     * 현재 로그인한 사용자를 위한 새 페르소나를 생성하고 저장
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param request 페르소나 생성 요청 DTO
     * @return 생성된 페르소나 응답 DTO
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public PersonaDto.Response createPersona(Long userId, PersonaDto.Request request) {
        // 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // 페르소나 생성 및 사용자 연결
        Persona persona = Persona.builder()
                .discType(request.getDiscType())
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .user(user)
                .build();

        // 추가: DISC 유형과 성별에 따라 프로필 이미지 URL 자동 할당
        try {
            // 프로필 이미지 URL 가져오기 
            String profileImageUrl = personaProfileImageService.getPersonaProfileImageUrl(
                    request.getDiscType(), 
                    request.getGender(), 
                    request.getAge()
            );
            
            // 페르소나에 프로필 이미지 URL 설정
            persona.setProfileImageUrl(profileImageUrl);
            log.info("페르소나 {} 생성 시 프로필 이미지 URL 설정: {}", persona.getName(), profileImageUrl);
        } catch (Exception e) {
            // 프로필 이미지 할당 중 오류 발생 시 로깅하고 계속 진행
            log.error("프로필 이미지 할당 중 오류 발생: {}", e.getMessage(), e);
            // 기본 이미지는 PersonaProfileImageService에서 처리
        }

        Persona savedPersona = personaRepository.save(persona);
        
        // 사용자 연결
        // userId와 personaId를 연결하는 UserPersona 저장
        UserPersona userPersona = UserPersona.builder()
                .user(user)
                .persona(savedPersona)
                .build();
        userPersonaRepository.save(userPersona);
        
        // 컨버터를 사용하여 엔티티를 DTO로 변환
        return personaConverter.toResponseDto(savedPersona);
    }

    /**
     * 현재 로그인한 사용자의 모든 페르소나 목록을 조회
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 페르소나 응답 DTO 목록
     */
    public List<PersonaDto.Response> getAllPersonasByUserId(Long userId) {
        List<UserPersona> userPersonas = userPersonaRepository.findByUserId(userId);

        return userPersonas.stream()
                .map(UserPersona::getPersona)
                .map(personaConverter::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 페르소나를 조회하고 현재 사용자의 소유권 확인
     *
     * @param id 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 페르소나 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    public PersonaDto.Response getPersonaByIdAndUserId(Long id, Long userId) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));
        
        // 현재 사용자가 페르소나의 소유자인지 확인
        boolean hasAccess = userPersonaRepository.findByUserIdAndPersonaId(userId, id).isPresent();

        if (!hasAccess) {
            throw new UnauthorizedAccessException("User does not have access to this persona");
        }
        
        return personaConverter.toResponseDto(persona);
    }

    /**
     * 이름으로 현재 로그인한 사용자의 페르소나 목록을 검색 (대소문자 구분 없음, 부분 일치)
     *
     * @param name 검색할 이름 (부분 또는 전체)
     * @param userId 현재 로그인한 사용자 ID
     * @return 검색된 페르소나 응답 DTO 목록
     */
    public List<PersonaDto.Response> searchPersonasByNameAndUserId(String name, Long userId) {
        if (!StringUtils.hasText(name)) {
            return Collections.emptyList();
        }

        List<UserPersona> userPersonas = userPersonaRepository.findByUserId(userId);
        return userPersonas.stream()
                .map(UserPersona::getPersona)
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(name.toLowerCase()))
                .map(personaConverter::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 기존 페르소나 정보를 업데이트하고 현재 사용자의 소유권 확인
     *
     * @param id 업데이트할 페르소나 ID
     * @param request 업데이트할 정보가 담긴 요청 DTO
     * @param userId 현재 로그인한 사용자 ID
     * @return 업데이트된 페르소나 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    @Transactional
    public PersonaDto.Response updatePersonaWithUserCheck(Long id, PersonaDto.Request request, Long userId) {
        Persona existingPersona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));

        // 현재 사용자가 페르소나의 소유자인지 확인
        boolean hasAccess = userPersonaRepository.findByUserIdAndPersonaId(userId, id).isPresent();

        if (!hasAccess) {
            throw new UnauthorizedAccessException("User does not have access to this persona");
        }

        // 컨버터를 사용하여 DTO 정보로 엔티티 업데이트
        Persona updatedPersona = personaConverter.updateEntityFromDto(existingPersona, request);
        
        // 추가: DISC 유형이나 성별이 변경된 경우 프로필 이미지 URL 업데이트
        try {
            // 프로필 이미지 URL 가져오기 (Gender enum 타입 그대로 사용)
            String profileImageUrl = personaProfileImageService.getPersonaProfileImageUrl(
                    updatedPersona.getDiscType(), 
                    updatedPersona.getGender(), // Gender enum 타입 그대로 전달
                    updatedPersona.getAge()
            );
            
            // 페르소나에 프로필 이미지 URL 설정
            updatedPersona.setProfileImageUrl(profileImageUrl);
            log.info("페르소나 {} 업데이트 시 프로필 이미지 URL 설정: {}", updatedPersona.getName(), profileImageUrl);
        } catch (Exception e) {
            // 프로필 이미지 할당 중 오류 발생 시 로깅하고 계속 진행
            log.error("프로필 이미지 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
        
        Persona savedPersona = personaRepository.save(updatedPersona);
        
        // 컨버터를 사용하여 엔티티를 DTO로 변환
        return personaConverter.toResponseDto(savedPersona);
    }

    /**
     * ID로 페르소나를 삭제하고 현재 사용자의 소유권 확인
     *
     * @param id 삭제할 페르소나 ID
     * @param userId 현재 로그인한 사용자 ID
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 현재 사용자가 해당 페르소나의 소유자가 아닌 경우
     */
    @Transactional
    public void deletePersonaWithUserCheck(Long id, Long userId) {
        log.info("페르소나 삭제 시작: personaId={}, userId={}", id, userId);
        
        // 페르소나 존재 여부 확인
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));
        
        // 현재 사용자가 페르소나의 소유자인지 확인
        boolean hasAccess = userPersonaRepository.findByUserIdAndPersonaId(userId, id).isPresent();

        if (!hasAccess) {
            throw new UnauthorizedAccessException("User does not have access to this persona");
        }
        
        try {
            // 1. 채팅 요약 데이터 삭제 - 인스턴스 메서드로 호출
            log.info("페르소나 {} 관련 채팅 요약 데이터 삭제 중...", id);
            chatSummaryRepository.deleteByPersonaId(id);
        
            // 2. 채팅 메시지 삭제 - 인스턴스 메서드로 호출
            log.info("페르소나 {} 관련 채팅 메시지 삭제 중...", id);
            chatMessageRepository.deleteByPersonaId(id);
        
            // 3. 사용자-페르소나 연결 정보 삭제 - 인스턴스 메서드로 호출
            log.info("페르소나 {} 관련 사용자 연결 정보 삭제 중...", id);
            userPersonaRepository.deleteByPersonaId(id);
        
            // 4. 마지막으로 페르소나 삭제
            log.info("페르소나 {} 삭제 중...", id);
            personaRepository.delete(persona);
        
            log.info("페르소나 {} 및 관련 데이터 삭제 완료", id);
        } catch (Exception e) {
            log.error("페르소나 {} 삭제 중 오류 발생: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
