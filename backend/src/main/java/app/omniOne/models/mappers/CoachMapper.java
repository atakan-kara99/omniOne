package app.omniOne.models.mappers;

import app.omniOne.models.dtos.CoachPatchDto;
import app.omniOne.models.dtos.CoachResponseDto;
import app.omniOne.models.entities.Coach;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CoachMapper {

    CoachResponseDto convert(Coach coach);

    void patch(CoachPatchDto dto, @MappingTarget Coach coach);

}
