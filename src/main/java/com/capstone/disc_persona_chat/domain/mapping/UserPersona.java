package com.capstone.disc_persona_chat.domain.mapping;

import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona; // 페르소나 소유자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 페르소나 소유자

    // 유저 페르소나와 채팅 메시지 간의 일대다 관계 설정
    @OneToMany(mappedBy = "userPersona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // 기본값이 무시되지 않도록 설정
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // 유저 페르소나와 채팅 요약 간의 일대다 관계 설정
    @OneToMany(mappedBy = "userPersona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatSummary> chatSummaries = new ArrayList<>();


    // 유저 페르소나에 채팅 메시지 추가
    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
        message.setUserPersona(this);
    }

    // 페르소나에서 채팅 메시지 제거
    public void removeChatMessage(ChatMessage message) {
        chatMessages.remove(message);
        message.setUserPersona(null);
    }

    // 페르소나에 채팅 요약 추가
    public void addChatSummary(ChatSummary summary) {
        chatSummaries.add(summary);
        summary.setUserPersona(this);
    }

    // 페르소나에서 채팅 요약 제거
    public void removeChatSummary(ChatSummary summary) {
        chatSummaries.remove(summary);
        summary.setUserPersona(null);
    }
}
