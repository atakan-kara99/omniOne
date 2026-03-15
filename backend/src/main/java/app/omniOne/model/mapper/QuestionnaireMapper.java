package app.omniOne.model.mapper;

import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionResponse;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuestionnaireMapper {

    QuestionnaireQuestionResponse map(QuestionnaireQuestion question);

    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "answerText", ignore = true)
    @Mapping(target = "questionText", ignore = true)
    QuestionnaireAnswerResponse map(QuestionnaireAnswer answer);

}
