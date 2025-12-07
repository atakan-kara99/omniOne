package app.omniOne.model.mapper;

import app.omniOne.model.dto.UserDto;
import app.omniOne.model.dto.UserProfileDto;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto map(User user);

    UserProfileDto map(UserProfile profile);

    void map(UserProfileRequest request, @MappingTarget UserProfile profile);

}
