package app.omniOne.model.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionnaireQuestionPostRequest(

        @NotBlank
        String text

) {}
