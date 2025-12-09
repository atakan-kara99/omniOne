package app.omniOne.model.dto;

import java.time.LocalDateTime;

public record NutriPlanResponse(

        Double calories,

        Double carbs,

        Double proteins,

        Double fats,

        Double water,

        Double salt,

        Double fiber,

        LocalDateTime createdAt

) {}
