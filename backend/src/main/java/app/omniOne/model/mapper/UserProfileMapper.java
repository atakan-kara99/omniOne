package app.omniOne.model.mapper;

import app.omniOne.model.dto.UserProfileDto;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileDto map(UserProfile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void map(UserProfileRequest request, @MappingTarget UserProfile profile);

}
