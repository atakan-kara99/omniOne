package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutritionPlanRequest;
import app.omniOne.model.dto.NutritionPlanResponse;
import app.omniOne.model.entity.NutritionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NutritionPlanMapper {

    NutritionPlanResponse map(NutritionPlan nutritionPlan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "calories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void map(NutritionPlanRequest request, @MappingTarget NutritionPlan nutritionPlan);

}
