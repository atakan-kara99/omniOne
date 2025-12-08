package app.omniOne.authentication.model;

import app.omniOne.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthDto map(User user);

}
