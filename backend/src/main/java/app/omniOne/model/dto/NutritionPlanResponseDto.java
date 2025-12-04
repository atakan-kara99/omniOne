package app.omniOne.model.dto;

import java.time.LocalDate;

public record NutritionPlanResponseDto(

        Integer calories,

        Integer carbohydrates,

        Integer proteins,

        Integer fats,

        LocalDate startDate,

        LocalDate endDate

) {}
