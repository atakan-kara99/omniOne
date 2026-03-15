package app.omniOne.model.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record NutritionPlanRequest(

        @NotNull
        @Range(min = 0, max = 2500)
        Integer carbs,

        @NotNull
        @Range(min = 0, max = 1000)
        Integer proteins,

        @NotNull
        @Range(min = 0, max = 500)
        Integer fats,

        @Range(min = 0, max = 50000)
        Integer water,

        @Range(min = 0, max = 50)
        Float salt,

        @Range(min = 0, max = 100)
        Float fiber

) {}
