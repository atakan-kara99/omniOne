package app.omniOne.chatting.model.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ChatParticipantId implements Serializable {

    private UUID conversationId;

    private UUID userId;

}
