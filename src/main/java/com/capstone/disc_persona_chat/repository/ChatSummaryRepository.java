package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.entity.ChatSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSummaryRepository extends JpaRepository<ChatSummary, Long> {

    // 특정 페르소나 ID에 대한 모든 채팅 요약을 타임스탬프 내림차순으로 반환환
    List<ChatSummary> findByPersonaIdOrderByTimestampDesc(Long personaId);

    // 특정 페르소나 ID에 대한 가장 최신 채팅 요약 하나를 찾기기
    Optional<ChatSummary> findTopByPersonaIdOrderByTimestampDesc(Long personaId);
}

