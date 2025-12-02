package app.omniOne.models.mappers;

import app.omniOne.models.entities.Client;
import app.omniOne.models.dtos.ClientDto;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDto toClientDto(Client client) {
        return new ClientDto(client.getId(), client.getEmail(), client.getStatus());
    }

}
