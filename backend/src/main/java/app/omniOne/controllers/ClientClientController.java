package app.omniOne.controllers;

import app.omniOne.models.dtos.ClientPatchDto;
import app.omniOne.models.dtos.ClientResponseDto;
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

    @GetMapping
    public ResponseEntity<ClientResponseDto> getClient(@PathVariable Long clientId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clientMapper.convert(clientService.getClient(clientId)));
    }

    @PatchMapping
    public ResponseEntity<ClientResponseDto> patchClient(
            @PathVariable Long clientId, @RequestBody @Valid ClientPatchDto patchDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clientMapper.convert(clientService.patchClient(clientId, patchDto)));
    }

}
