package app.omniOne.chatting.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatMessageRequest(

        @NotNull
        UUID clientMessageId,

        UUID to,

        @NotBlank
        @Size(max = 10000)
        String content

) {}
