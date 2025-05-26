package com.capstone.disc_persona_chat.converter.impl;

import com.capstone.disc_persona_chat.converter.PersonaConverter;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import com.capstone.disc_persona_chat.dto.PersonaDto;
import org.springframework.stereotype.Component;

/**
 * PersonaConverter 인터페이스의 구현 클래스
 */
@Component
public class PersonaConverterImpl implements PersonaConverter {

    /**
     * Persona 엔티티를 PersonaDto.Response로 변환
     * @param entity Persona 엔티티
     * @return 변환된 PersonaDto.Response
     */
    @Override
    public PersonaDto.Response toResponseDto(Persona entity) {
        if (entity == null) {
            return null;
        }
        
        return PersonaDto.Response.builder()
                .id(entity.getId())
                .discType(entity.getDiscType())
                //.userid(entity.getUser().getId())
                .name(entity.getName())
                .age(entity.getAge())
                .gender(entity.getGender())
                .build();
    }
    
    /**
     * PersonaDto.Request를 Persona 엔티티로 변환
     * @param dto PersonaDto.Request
     * @param user 페르소나 소유자 (Users 엔티티)
     * @return 변환된 Persona 엔티티
     */
    @Override
    public Persona toEntity(PersonaDto.Request dto, Users user) {
        if (dto == null) {
            return null;
        }
        
        return Persona.builder()
                .discType(dto.getDiscType())
                .name(dto.getName())
                .age(dto.getAge())
                .gender(dto.getGender())
                //.user(user)
                .build();
    }
    
    /**
     * 기존 Persona 엔티티를 PersonaDto.Request 정보로 업데이트
     * @param entity 업데이트할 Persona 엔티티
     * @param dto 업데이트 정보가 담긴 PersonaDto.Request
     * @return 업데이트된 Persona 엔티티
     */
    @Override
    public Persona updateEntityFromDto(Persona entity, PersonaDto.Request dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        
        // 필드 업데이트
        if (dto.getDiscType() != null) {
            entity.setDiscType(dto.getDiscType());
        }
        
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        
        if (dto.getAge() != null) {
            entity.setAge(dto.getAge());
        }
        
        if (dto.getGender() != null) {
            entity.setGender(dto.getGender());
        }
        
        return entity;
    }
}
