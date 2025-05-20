package com.capstone.disc_persona_chat.domain.entity;

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
public class ChatSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요약이 속한 페르소나와의 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Lob
    @Column(nullable = false)
    private String summaryText; // 대화 요약 텍스트

    private Integer score; // 대화 점수 

    @Column(columnDefinition = "LONGTEXT")
    private String corePoints; // 핵심 긍정 포인트

    @Column(columnDefinition = "LONGTEXT")
    private String improvements; // 개선점

    @Column(columnDefinition = "LONGTEXT")
    private String tips; // 대화 팁

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp; // 요약 생성 타임스탬프
}
