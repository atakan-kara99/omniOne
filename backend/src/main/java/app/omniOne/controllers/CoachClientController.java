package app.omniOne.controllers;

import app.omniOne.models.dtos.ClientDto;
import app.omniOne.models.entities.Client;
import app.omniOne.models.mappers.ClientMapper;
import app.omniOne.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/coach/{coachId}")
public class CoachClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public CoachClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    @PostMapping("/clients")
    public ResponseEntity<ClientDto> registerClient(@PathVariable Long coachId, @RequestBody @Valid Client client) {
        Client registeredClient = clientService.registerClient(coachId, client);
        ClientDto registeredClientDto = clientMapper.toClientDto(registeredClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredClientDto);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientDto>> getClients(@PathVariable Long coachId) {
        List<Client> clients = clientService.getClients(coachId);
        List<ClientDto> clientDtos = clients.stream().map(clientMapper::toClientDto).toList();
        return ResponseEntity.status(HttpStatus.OK).body(clientDtos);
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Long coachId, @PathVariable Long clientId) {
        ClientDto client = clientMapper.toClientDto(clientService.getClient(coachId, clientId));
        return ResponseEntity.status(HttpStatus.OK).body(client);
    }

}
