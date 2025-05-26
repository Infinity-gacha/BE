package com.capstone.disc_persona_chat.domain.entity;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.capstone.disc_persona_chat.Enums.DiscType;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 데이터베이스에 위임
    private Long id;

    @Enumerated(EnumType.STRING) // Enum 이름을 문자열로 데이터베이스에 저장
    @Column(nullable = false) // null 허용 안 함
    private DiscType discType;

    @Column(nullable = false)
    private String name;

    private Integer age; // 나이는 선택 사항이므로 nullable

    private String gender; // 성별도 선택 사항이므로 nullable

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private Users user; // 페르소나 소유자

    // 페르소나와 유저 페르소나 간의 일대다 관계 설정
    @OneToMany(mappedBy = "persona")
    @Builder.Default
    private List<UserPersona> userPersonas = new ArrayList<>();

 
}
