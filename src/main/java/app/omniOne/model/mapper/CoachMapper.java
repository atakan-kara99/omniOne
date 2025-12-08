package app.omniOne.model.mapper;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.dto.CoachResponseDto;
import app.omniOne.model.entity.Coach;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CoachMapper {

    CoachResponseDto map(Coach coach);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clients", ignore = true)
    void map(CoachPatchRequest dto, @MappingTarget Coach coach);

}
