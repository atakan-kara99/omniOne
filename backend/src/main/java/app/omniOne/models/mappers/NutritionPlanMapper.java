package app.omniOne.models.mappers;

import app.omniOne.models.dtos.NutritionPlanResponseDto;
import app.omniOne.models.entities.NutritionPlan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NutritionPlanMapper {

    NutritionPlanResponseDto convert(NutritionPlan nutritionPlan);

    //void patch(CoachPatchDto dto, @MappingTarget Coach coach);

}
