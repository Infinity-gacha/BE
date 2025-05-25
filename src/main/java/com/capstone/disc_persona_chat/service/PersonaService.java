package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.dto.PersonaDto;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.exception.PersonaNotFoundException;
import com.capstone.disc_persona_chat.exception.UnauthorizedAccessException;
import com.capstone.disc_persona_chat.exception.UserNotFoundException;
import com.capstone.disc_persona_chat.repository.PersonaRepository;
import com.capstone.disc_persona_chat.repository.UserRepository;
import com.capstone.disc_persona_chat.converter.PersonaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;
    private final PersonaConverter personaConverter;

    @Autowired
    public PersonaService(
            PersonaRepository personaRepository,
            UserRepository userRepository,
            PersonaConverter personaConverter) {
        this.personaRepository = personaRepository;
        this.userRepository = userRepository;
        this.personaConverter = personaConverter;
    }

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
                .user(user)
                .discType(request.getDiscType())
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .build();
                
        Persona savedPersona = personaRepository.save(persona);
        
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
        List<Persona> personas = personaRepository.findByUserId(userId);
        return personas.stream()
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
        if (!persona.getUser().getId().equals(userId)) {
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
        // 검색어가 비어있거나 null이면 빈 목록 반환
        if (!StringUtils.hasText(name)) {
            return Collections.emptyList();
        }
        List<Persona> personas = personaRepository.findByNameContainingIgnoreCaseAndUserId(name, userId);
        return personas.stream()
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
        if (!existingPersona.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User does not have access to this persona");
        }

        // 컨버터를 사용하여 DTO 정보로 엔티티 업데이트
        Persona updatedPersona = personaConverter.updateEntityFromDto(existingPersona, request);
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
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));
        
        // 현재 사용자가 페르소나의 소유자인지 확인
        if (!persona.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User does not have access to this persona");
        }
        
        // 연관된 ChatMessage 및 ChatSummary는 Persona 엔티티의 cascade 설정에 따라 함께 삭제
        personaRepository.delete(persona);
    }
}
