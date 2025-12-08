package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nutrition_plan")
public class NutriPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Integer carbs;

    @Column(nullable = false)
    private Integer proteins;

    @Column(nullable = false)
    private Integer fats;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null = active plan

    public boolean isActive() {
        return endDate == null;
    }

    @PrePersist
    @PreUpdate
    private void computeCalories() {
        this.calories = (int) Math.round(carbs * 4.1 + proteins * 4.1 + fats * 9.3);
    }

    public NutriPlan(Integer carbs, Integer proteins, Integer fats, Client client) {
        this.carbs = carbs;
        this.proteins = proteins;
        this.fats = fats;
        this.startDate = LocalDate.now();
        this.endDate = null;
        this.client = client;
    }

}
