package app.omniOne.models.mappers;

import app.omniOne.models.dtos.NutritionPlanDto;
import app.omniOne.models.entities.NutritionPlan;
import org.springframework.stereotype.Component;

@Component
public class NutritionPlanMapper {

    public NutritionPlanDto toNutritionPlanDto(NutritionPlan nutritionPlan) {
        return new NutritionPlanDto(
                nutritionPlan.getCalories(),
                nutritionPlan.getCarbohydrates(),
                nutritionPlan.getProteins(),
                nutritionPlan.getFats());
    }

}
