package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
}
