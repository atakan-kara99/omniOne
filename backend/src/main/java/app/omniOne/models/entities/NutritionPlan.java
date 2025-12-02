package app.omniOne.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nutrition_plan")
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer calories;

    @NotNull
    @Column(nullable = false)
    private Integer carbohydrates;

    @NotNull
    @Column(nullable = false)
    private Integer proteins;

    @NotNull
    @Column(nullable = false)
    private Integer fats;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null = active plan

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public boolean isActive() {
        return endDate == null;
    }

    @PrePersist
    @PreUpdate
    private void computeCalories() {
        this.calories = (int) Math.round(carbohydrates * 4.1 + proteins * 4.1 + fats * 9.3);
    }
}
