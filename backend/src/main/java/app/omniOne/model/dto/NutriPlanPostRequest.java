package app.omniOne.model.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record NutriPlanPostRequest(

        @Range(min = 0, max = 2500)
        @NotNull
        Integer carbs,

        @Range(min = 0, max = 1000)
        @NotNull
        Integer proteins,

        @Range(min = 0, max = 500)
        @NotNull
        Integer fats,

        @Range(min = 0, max = 10000)
        Integer water,

        @Range(min = 0, max = 25000)
        Integer salt,

        @Range(min = 0, max = 50000)
        Integer fiber

) {}
