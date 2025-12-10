package app.omniOne.model.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record NutritionPlanRequest(

        @Range(min = 0, max = 2500)
        @NotNull
        Double carbs,

        @Range(min = 0, max = 1000)
        @NotNull
        Double proteins,

        @Range(min = 0, max = 500)
        @NotNull
        Double fats,

        @Range(min = 0, max = 50)
        Double water,

        @Range(min = 0, max = 50)
        Double salt,

        @Range(min = 0, max = 100)
        Double fiber

) {}
