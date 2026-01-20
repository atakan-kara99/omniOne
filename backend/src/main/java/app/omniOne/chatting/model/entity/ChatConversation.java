package app.omniOne.chatting.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "chat_conversation")
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants;

    @OneToMany(mappedBy = "conversation")
    private List<ChatMessage> messages;

    @Column(length = 80)
    private String lastMessagePreview;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime lastMessageAt;

    @PrePersist
    @PreUpdate
    private void trimMessage() {
        final int LENGTH = 80;
        if (lastMessagePreview != null && lastMessagePreview.length() > LENGTH)
            lastMessagePreview = lastMessagePreview.substring(0, LENGTH - 3) + "..";
    }

}