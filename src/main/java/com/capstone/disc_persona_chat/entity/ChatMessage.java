package com.capstone.disc_persona_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지가 속한 페르소나와의 다대일 관계
    // FetchType.LAZY: 연관된 페르소나 엔티티를 필요할 때만 로드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false) // 외래 키 컬럼 설정
    private Persona persona;

    @Lob // 대용량 텍스트 데이터를 저장하기 위한 어노테이션
    @Column(nullable = false)
    private String content=""; // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType; // 발신자 유형 (USER 또는 AI)

    private String emotion; // AI 응답의 감정

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
    @Column(nullable = false, updatable = false) // null 불가, 업데이트 불가
    private LocalDateTime timestamp;
}
