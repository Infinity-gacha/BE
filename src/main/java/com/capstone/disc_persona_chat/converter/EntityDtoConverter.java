package com.capstone.disc_persona_chat.converter;

/**
 * 엔티티와 DTO 간의 변환을 위한 공통 인터페이스
 * @param <E> 엔티티 타입
 * @param <D> DTO 타입
 */
public interface EntityDtoConverter<E, D> {
    
    /**
     * 엔티티를 DTO로 변환
     * @param entity 변환할 엔티티
     * @return 변환된 DTO
     */
    D toDto(E entity);
    
    /**
     * DTO를 엔티티로 변환
     * @param dto 변환할 DTO
     * @return 변환된 엔티티
     */
    E toEntity(D dto);
}
