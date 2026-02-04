package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "nutrition_plan")
public class NutritionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private Integer carbs;

    @Column(nullable = false)
    private Integer proteins;

    @Column(nullable = false)
    private Integer fats;

    @Column(nullable = false)
    private Integer calories;

    private Integer water;

    private Float salt;

    private Float fiber;

    @PrePersist
    @PreUpdate
    private void computeCalories() {
        this.calories = (int) (carbs * 4.1 + proteins * 4.1 + fats * 9.3);
    }

}
