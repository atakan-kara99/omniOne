package app.omniOne.model.dto;

public record QuestionnaireAnswerResponse(

        Long questionId,

        String questionText,

        String answerText

) {}
