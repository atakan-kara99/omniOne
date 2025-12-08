package app.omniOne.model.mapper;

import app.omniOne.model.dto.NutriPlanResponseDto;
import app.omniOne.model.entity.NutriPlan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NutriPlanMapper {

    NutriPlanResponseDto map(NutriPlan nutriPlan);

}
