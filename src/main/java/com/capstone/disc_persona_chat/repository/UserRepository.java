package com.capstone.disc_persona_chat.repository;

import com.capstone.disc_persona_chat.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    
    /**
     * 이메일 존재 여부 확인
     *
     * @param email 확인할 이메일
     * @return 이메일이 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임 존재 여부 확인
     *
     * @param name 확인할 닉네임
     * @return 닉네임이 존재하면 true, 없으면 false
     */
    boolean existsByName(String name);
}
