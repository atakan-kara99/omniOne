package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.dto.NutriPlanResponseDto;
import app.omniOne.model.entity.NutriPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NutriPlanMapper {

    NutriPlanResponseDto map(NutriPlan nutriPlan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "calories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void map(NutriPlanPostRequest request, @MappingTarget NutriPlan nutriPlan);

}
