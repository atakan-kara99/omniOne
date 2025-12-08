package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutriPlanPostRequest;
import app.omniOne.model.dto.NutriPlanResponseDto;
import app.omniOne.model.entity.NutriPlan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NutriPlanMapper {

    NutriPlanResponseDto map(NutriPlan nutriPlan);

    void map(NutriPlanPostRequest request, @MappingTarget NutriPlan nutriPlan);

}
