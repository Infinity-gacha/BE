package com.capstone.disc_persona_chat.service;

import com.capstone.disc_persona_chat.dto.PersonaDto;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.exception.PersonaNotFoundException;
import com.capstone.disc_persona_chat.repository.PersonaRepository;
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
    private final PersonaConverter personaConverter;

    @Autowired
    public PersonaService(
            PersonaRepository personaRepository,
            PersonaConverter personaConverter) {
        this.personaRepository = personaRepository;
        this.personaConverter = personaConverter;
    }

    /**
     * 새 페르소나를 생성하고 저장
     *
     * @param request 페르소나 생성 요청 DTO
     * @return 생성된 페르소나 응답 DTO
     */
    @Transactional
    public PersonaDto.Response createPersona(PersonaDto.Request request) {
        // 컨트롤러에서 userId를 제공하지 않으므로, 여기서는 사용자 정보 없이 페르소나 생성
        // 실제 구현에서는 인증된 사용자 정보를 가져오는 로직이 필요할 수 있음
        
        // 컨버터를 사용하여 DTO를 엔티티로 변환 (사용자 정보 없이)
        Persona persona = Persona.builder()
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
     * 모든 페르소나 목록을 조회
     *
     * @return 페르소나 응답 DTO 목록
     */
    public List<PersonaDto.Response> getAllPersonas() {
        List<Persona> personas = personaRepository.findAll();
        return personas.stream()
                .map(personaConverter::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 페르소나를 조회
     *
     * @param id 페르소나 ID
     * @return 페르소나 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    public PersonaDto.Response getPersonaById(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));
        return personaConverter.toResponseDto(persona);
    }

    /**
     * 이름으로 페르소나 목록을 검색 (대소문자 구분 없음, 부분 일치).
     *
     * @param name 검색할 이름 (부분 또는 전체)
     * @return 검색된 페르소나 응답 DTO 목록
     */
    public List<PersonaDto.Response> searchPersonasByName(String name) {
        // 검색어가 비어있거나 null이면 빈 목록 반환
        if (!StringUtils.hasText(name)) {
            return Collections.emptyList();
        }
        List<Persona> personas = personaRepository.findByNameContainingIgnoreCase(name);
        return personas.stream()
                .map(personaConverter::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 기존 페르소나 정보를 업데이트
     *
     * @param id      업데이트할 페르소나 ID
     * @param request 업데이트할 정보가 담긴 요청 DTO
     * @return 업데이트된 페르소나 응답 DTO
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional
    public PersonaDto.Response updatePersona(Long id, PersonaDto.Request request) {
        Persona existingPersona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));

        // 컨버터를 사용하여 DTO 정보로 엔티티 업데이트
        Persona updatedPersona = personaConverter.updateEntityFromDto(existingPersona, request);
        Persona savedPersona = personaRepository.save(updatedPersona);
        
        // 컨버터를 사용하여 엔티티를 DTO로 변환
        return personaConverter.toResponseDto(savedPersona);
    }

    /**
     * ID로 페르소나를 삭제
     *
     * @param id 삭제할 페르소나 ID
     * @throws PersonaNotFoundException 페르소나를 찾을 수 없는 경우
     */
    @Transactional
    public void deletePersona(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException("Persona not found with id: " + id));
        // 연관된 ChatMessage 및 ChatSummary는 Persona 엔티티의 cascade 설정에 따라 함께 삭제
        personaRepository.delete(persona);
    }
}
