package app.omniOne.model.mapper;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse map(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "nutritionPlans", ignore = true)
    void map(ClientPatchRequest dto, @MappingTarget Client client);

}
