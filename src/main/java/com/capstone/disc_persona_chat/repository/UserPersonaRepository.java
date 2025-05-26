package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPersonaRepository extends JpaRepository<UserPersona, Long> {
    Optional<UserPersona> findByUserIdAndPersonaId(Long userId, Long personaId);
    List<UserPersona> findByUserId(Long userId);
}
