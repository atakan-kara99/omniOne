package app.omniOne.model.mapper;

import app.omniOne.model.dto.CoachPatchRequest;
import app.omniOne.model.dto.CoachResponse;
import app.omniOne.model.entity.Coach;
import app.omniOne.model.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CoachMapper {

    CoachResponse map(Coach coach);

    @Mapping(target = "id", source = "coach.id")
    CoachResponse map(Coach coach, UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "clients", ignore = true)
    void map(CoachPatchRequest dto, @MappingTarget Coach coach);

}
