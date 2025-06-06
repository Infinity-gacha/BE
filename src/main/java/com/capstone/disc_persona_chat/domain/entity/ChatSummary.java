package com.capstone.disc_persona_chat.domain.entity;

import com.capstone.disc_persona_chat.domain.mapping.UserPersona;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UserPersona와의 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_persona_id", nullable = false)
    private UserPersona userPersona;

    @Lob
    @Column(nullable = false)
    private String summaryText;

    private Integer score;

    @Column(columnDefinition = "LONGTEXT")
    private String corePoints;

    @Column(columnDefinition = "LONGTEXT")
    private String improvements;

    @Column(columnDefinition = "LONGTEXT")
    private String tips;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
