package app.omniOne.model.dto;

import jakarta.validation.constraints.NotNull;

public record QuestionnaireAnswerRequest(

        @NotNull
        Long questionId,

        String answer

) {}
