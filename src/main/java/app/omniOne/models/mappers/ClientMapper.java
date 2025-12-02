package app.omniOne.models.mappers;

import app.omniOne.models.dtos.ClientPatchDto;
import app.omniOne.models.dtos.ClientResponseDto;
import app.omniOne.models.entities.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponseDto convert(Client client);

    void patch(ClientPatchDto dto, @MappingTarget Client client);

}
