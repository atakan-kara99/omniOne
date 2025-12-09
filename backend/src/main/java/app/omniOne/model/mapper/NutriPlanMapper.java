package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutriPlanRequest;
import app.omniOne.model.dto.NutriPlanResponse;
import app.omniOne.model.entity.NutriPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NutriPlanMapper {

    NutriPlanResponse map(NutriPlan nutriPlan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "calories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void map(NutriPlanRequest request, @MappingTarget NutriPlan nutriPlan);

}
