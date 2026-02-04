package app.omniOne.model.dto;

import java.time.LocalDateTime;

public record NutritionPlanResponse(

        Long id,

        Integer calories,

        Integer carbs,

        Integer proteins,

        Integer fats,

        Integer water,

        Float salt,

        Float fiber,

        LocalDateTime createdAt

) {}
