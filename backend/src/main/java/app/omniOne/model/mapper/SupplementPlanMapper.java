package app.omniOne.model.mapper;

import app.omniOne.model.dto.SupplementPlanResponse;
import app.omniOne.model.entity.SupplementPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SupplementEntryMapper.class})
public interface SupplementPlanMapper {

    @Mapping(source = "supplementEntries", target = "entries")
    SupplementPlanResponse map(SupplementPlan plan);

}
