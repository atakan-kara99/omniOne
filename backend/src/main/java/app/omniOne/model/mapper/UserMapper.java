package app.omniOne.model.mapper;

import app.omniOne.model.dto.UserDto;
import app.omniOne.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto map(User user);

}
