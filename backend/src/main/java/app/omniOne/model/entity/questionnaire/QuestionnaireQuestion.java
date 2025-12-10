package app.omniOne.model.entity.questionnaire;

import app.omniOne.model.entity.Coach;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "questionnaire_question")
public class QuestionnaireQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private Coach coach;    // If null → standard question

    @Column(nullable = false)
    private String text;

}
