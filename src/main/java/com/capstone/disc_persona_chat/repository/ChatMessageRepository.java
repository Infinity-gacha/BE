package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 UserPersona ID에 대한 모든 채팅 메시지를 타임스탬프 오름차순으로 반환
    List<ChatMessage> findByUserPersonaIdOrderByTimestampAsc(Long userPersonaId);
    
    // 특정 Persona ID와 연관된 모든 채팅 메시지를 타임스탬프 오름차순으로 반환
    List<ChatMessage> findByUserPersona_Persona_IdOrderByTimestampAsc(Long personaId);

    // 특정 UserPersona ID에 대한 채팅 메시지 수를 계산
    long countByUserPersonaId(Long userPersonaId);
    
    // 특정 Persona ID와 연관된 채팅 메시지 수를 계산
    long countByUserPersona_Persona_Id(Long personaId);

    // 페르소나 ID로 관련 ChatMessage 레코드 삭제 (추가)
    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.userPersona.persona.id = :personaId")
    void deleteByPersonaId(@Param("personaId") Long personaId);
}
