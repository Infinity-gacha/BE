package com.capstone.disc_persona_chat.converter;

import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.PersonaDto;

/**
 * Persona 엔티티와 PersonaDto 간의 변환을 위한 컨버터 인터페이스
 */
public interface PersonaConverter {
    
    /**
     * Persona 엔티티를 PersonaDto.Response로 변환
     * @param entity Persona 엔티티
     * @return 변환된 PersonaDto.Response
     */
    PersonaDto.Response toResponseDto(Persona entity);
    
    /**
     * PersonaDto.Request를 Persona 엔티티로 변환
     * @param dto PersonaDto.Request
     * @param user 페르소나 소유자 (Users 엔티티)
     * @return 변환된 Persona 엔티티
     */
    Persona toEntity(PersonaDto.Request dto, Users user);
    
    /**
     * 기존 Persona 엔티티를 PersonaDto.Request 정보로 업데이트
     * @param entity 업데이트할 Persona 엔티티
     * @param dto 업데이트 정보가 담긴 PersonaDto.Request
     * @return 업데이트된 Persona 엔티티
     */
    Persona updateEntityFromDto(Persona entity, PersonaDto.Request dto);
}
