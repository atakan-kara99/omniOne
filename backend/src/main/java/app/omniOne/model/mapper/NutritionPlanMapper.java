package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutritionPlanResponseDto;
import app.omniOne.model.entity.NutritionPlan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NutritionPlanMapper {

    NutritionPlanResponseDto map(NutritionPlan nutritionPlan);

}
