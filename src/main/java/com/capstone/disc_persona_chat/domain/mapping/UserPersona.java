package com.capstone.disc_persona_chat.domain.mapping;

import com.capstone.disc_persona_chat.domain.entity.ChatMessage;
import com.capstone.disc_persona_chat.domain.entity.ChatSummary;
import com.capstone.disc_persona_chat.domain.entity.Persona;
import com.capstone.disc_persona_chat.domain.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;
    
    // ChatSummary와의 일대다 관계
    @OneToMany(mappedBy = "userPersona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatSummary> chatSummaries = new ArrayList<>();
    
    // ChatMessage와의 일대다 관계 추가
    @OneToMany(mappedBy = "userPersona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();
    
    public void addChatSummary(ChatSummary chatSummary) {
        chatSummaries.add(chatSummary);
        chatSummary.setUserPersona(this);
    }
    
    public void addChatMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        chatMessage.setUserPersona(this);
    }
}
