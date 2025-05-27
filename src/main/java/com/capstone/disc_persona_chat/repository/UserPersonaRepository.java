package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPersonaRepository extends JpaRepository<UserPersona, Long> {
    // 사용자 ID와 페르소나 ID로 UserPersona 찾기
    Optional<UserPersona> findByUserIdAndPersonaId(Long userId, Long personaId);
    
    // 사용자 ID로 모든 UserPersona 찾기
    List<UserPersona> findByUserId(Long userId);
    
    // 페르소나 ID로 모든 UserPersona 찾기
    List<UserPersona> findByPersonaId(Long personaId);
    
    // 사용자 ID로 UserPersona 존재 여부 확인
    boolean existsByUserId(Long userId);
    
    // 페르소나 ID로 UserPersona 존재 여부 확인
    boolean existsByPersonaId(Long personaId);

    // 페르소나 ID로 관련 UserPersona 레코드 삭제
    @Modifying
    @Query("DELETE FROM UserPersona up WHERE up.persona.id = :personaId")
    void deleteByPersonaId(@Param("personaId") Long personaId);
}
