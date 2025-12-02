package app.omniOne.controllers;

import app.omniOne.models.dtos.ClientDto;
import app.omniOne.models.entities.Client;
import app.omniOne.models.mappers.ClientMapper;
import app.omniOne.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/{clientId}")
public class ClientClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    @PatchMapping
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long clientId, @RequestBody @Valid Client client) {
        ClientDto cd = clientMapper.toClientDto(clientService.updateClient(clientId, client));
        return ResponseEntity.status(HttpStatus.OK).body(cd);
    }

}
