package app.omniOne.models.dtos;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record NutritionPlanPostDto(

        @Range(min = 0, max = 1000)
        @NotNull
        Integer carbohydrates,

        @Range(min = 0, max = 1000)
        @NotNull
        Integer proteins,

        @Range(min = 0, max = 1000)
        @NotNull
        Integer fats) {
}
