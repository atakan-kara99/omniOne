package app.omniOne.model.mapper;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.entity.Client;
import app.omniOne.model.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "countryCode", ignore = true)
    @Mapping(target = "city", ignore = true)
    ClientResponse map(Client client);

    @Mapping(target = "id", source = "client.id")
    @Mapping(target = "firstName", source = "userProfile.firstName")
    @Mapping(target = "lastName", source = "userProfile.lastName")
    @Mapping(target = "birthDate", source = "userProfile.birthDate")
    @Mapping(target = "gender", source = "userProfile.gender")
    @Mapping(target = "countryCode", source = "userProfile.countryCode")
    @Mapping(target = "city", source = "userProfile.city")
    ClientResponse map(Client client, UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "nutritionPlans", ignore = true)
    void map(ClientPatchRequest dto, @MappingTarget Client client);

}
