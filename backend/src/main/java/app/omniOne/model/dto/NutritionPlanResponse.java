package app.omniOne.model.dto;

import java.time.LocalDateTime;

public record NutritionPlanResponse(

        Double calories,

        Double carbs,

        Double proteins,

        Double fats,

        Double water,

        Double salt,

        Double fiber,

        LocalDateTime createdAt

) {}
