package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 페르소나 ID에 대한 모든 채팅 메시지를 타임스탬프 오름차순으로 반환환
    List<ChatMessage> findByPersonaIdOrderByTimestampAsc(Long personaId);

    // 특정 페르소나 ID에 대한 채팅 메시지 수를 계산
    long countByPersonaId(Long personaId);
}

