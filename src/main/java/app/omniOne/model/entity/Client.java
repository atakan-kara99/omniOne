package app.omniOne.model.entity;

import app.omniOne.exception.custom.ResourceNotFoundException;
import app.omniOne.model.entity.questionnaire.QuestionnaireAnswer;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "client")
public class Client extends BaseEntity {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private Coach coach;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NutritionPlan> nutritionPlans;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionnaireAnswer> answers;

    public Coach getCoachOrThrow() {
        if (coach == null)
            throw new ResourceNotFoundException("Coach not found");
        return coach;
    }

}
