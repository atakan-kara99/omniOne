package app.omniOne.controllers;

import app.omniOne.models.dtos.ClientPostDto;
import app.omniOne.models.dtos.ClientResponseDto;
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
    public ResponseEntity<ClientResponseDto> registerClient(
            @PathVariable Long coachId, @RequestBody @Valid ClientPostDto postDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientMapper.convert(clientService.registerClient(coachId, postDto)));
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponseDto>> getClients(@PathVariable Long coachId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clientService.getClients(coachId).stream().map(clientMapper::convert).toList());
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientResponseDto> getClient(@PathVariable Long coachId, @PathVariable Long clientId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clientMapper.convert(clientService.getClient(coachId, clientId)));
    }

}
