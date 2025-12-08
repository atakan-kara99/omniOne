package app.omniOne.model.dto;

import java.time.LocalDateTime;

public record NutriPlanResponseDto(

        Integer calories,

        Integer carbs,

        Integer proteins,

        Integer fats,

        Integer water,

        Integer salt,

        Integer fiber,

        LocalDateTime createdAt

) {}
