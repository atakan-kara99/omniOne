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

    @OrderBy("sentAt DESC")
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<ChatMessage> messages;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime lastMessageAt;

}