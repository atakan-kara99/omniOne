package app.omniOne.model.mapper;

import app.omniOne.model.dto.ClientPatchDto;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponseDto map(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "nutritionPlans", ignore = true)
    void map(ClientPatchDto dto, @MappingTarget Client client);

}
