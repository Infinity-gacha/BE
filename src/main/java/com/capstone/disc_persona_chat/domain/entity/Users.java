package com.capstone.disc_persona_chat.domain.entity;

import com.capstone.disc_persona_chat.Enums.Gender;
import com.capstone.disc_persona_chat.Enums.Role;
import com.capstone.disc_persona_chat.domain.common.BaseEntity;
import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    private String socialType;

    // 유저와 유저 페르소나 간의 일대다 관계 설정
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<UserPersona> userPersonas = new ArrayList<>();

    public void encodePassword(String password) {
        this.password = password;
    }

}
