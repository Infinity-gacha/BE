package com.capstone.disc_persona_chat.domain.entity;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.domain.common.BaseEntity;
import jakarta.persistence.*;

@Entity
public class Users extends BaseEntity {
    @Id
    Long id;

    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    @Enumerated(EnumType.STRING)
    Gender gender;

    String socialType;

}
