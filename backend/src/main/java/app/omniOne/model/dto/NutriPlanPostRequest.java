package app.omniOne.model.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record NutriPlanPostRequest(

        @Range(min = 0, max = 2000)
        @NotNull
        Integer carbohydrates,

        @Range(min = 0, max = 1000)
        @NotNull
        Integer proteins,

        @Range(min = 0, max = 500)
        @NotNull
        Integer fats) {
}
