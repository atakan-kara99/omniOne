package app.omniOne.model.mapper;

import app.omniOne.model.dto.QuestionnaireAnswerResponse;
import app.omniOne.model.dto.QuestionnaireQuestionResponse;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import app.omniOne.model.entity.questionnaire.QuestionnaireQuestion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionnaireMapper {

    QuestionnaireQuestionResponse map(QuestionnaireQuestion question);

    QuestionnaireAnswerResponse map(QuestionnaireAnswer answer);

}
