package app.omniOne.model.dto;

import java.time.LocalDate;

public record NutriPlanResponseDto(

        Integer calories,

        Integer carbs,

        Integer proteins,

        Integer fats,

        LocalDate startDate,

        LocalDate endDate

) {}
