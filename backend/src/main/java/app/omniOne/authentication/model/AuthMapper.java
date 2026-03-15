package app.omniOne.authentication.model;

import app.omniOne.authentication.model.dto.AuthResponse;
import app.omniOne.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthResponse map(User user);

}
