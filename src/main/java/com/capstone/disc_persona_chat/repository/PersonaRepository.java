package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.entity.Persona;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

     /**
     * 이름에 특정 문자열을 포함하는 페르소나 목록을 대소문자 구분 없이 반환환
     * @param name 검색할 이름의 일부
     * @return 이름 조건을 만족하는 페르소나 목록
     */
    List<Persona> findByNameContainingIgnoreCase(String name);

    // 사용자 ID로 페르소나 목록 조회
    //List<Persona> findByUserId(Long userId);
    
    // 이름과 사용자 ID로 페르소나 검색 (대소문자 구분 없음)
    //List<Persona> findByNameContainingIgnoreCaseAndUserId(String name, Long userId);
}

