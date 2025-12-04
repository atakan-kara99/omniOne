package app.omniOne.controller;

import app.omniOne.model.dto.ClientPatchDto;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/{clientId}")
public class ClientClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient(@PathVariable Long clientId) {
        return clientMapper.map(clientService.getClient(clientId));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto patchClient(@PathVariable Long clientId, @RequestBody @Valid ClientPatchDto dto) {
        return clientMapper.map(clientService.patchClient(clientId, dto));
    }

}
