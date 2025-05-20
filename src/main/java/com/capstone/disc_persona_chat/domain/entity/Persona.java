package com.capstone.disc_persona_chat.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // 페르소나와 채팅 메시지 간의 일대다 관계 설정
    // mappedBy = "persona": ChatMessage 엔티티의 'persona' 필드가 이 관계의 주인임을 명시
    // cascade = CascadeType.ALL: 페르소나 관련 작업(저장, 삭제 등)이 ChatMessage에도 전파됨
    // orphanRemoval = true: 부모(Persona)와의 관계가 끊어진 자식(ChatMessage) 엔티티를 자동으로 삭제
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // 페르소나와 채팅 요약 간의 일대다 관계 설정 (위와 유사)
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatSummary> chatSummaries = new ArrayList<>();

    // 페르소나에 채팅 메시지 추가
    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
        message.setPersona(this);
    }

    // 페르소나에서 채팅 메시지 제거
    public void removeChatMessage(ChatMessage message) {
        chatMessages.remove(message);
        message.setPersona(null);
    }

    // 페르소나에 채팅 요약 추가
    public void addChatSummary(ChatSummary summary) {
        chatSummaries.add(summary);
        summary.setPersona(this);
    }

    // 페르소나에서 채팅 요약 제거
    public void removeChatSummary(ChatSummary summary) {
        chatSummaries.remove(summary);
        summary.setPersona(null);
    }
}

