package app.omniOne.controller;

import app.omniOne.model.dto.ClientPatchDto;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/{clientId}")
@PreAuthorize("@authService.isOwner(#clientId)")
public class ClientClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient(@PathVariable UUID clientId) {
        return clientMapper.map(clientService.getClient(clientId));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto patchClient(@PathVariable UUID clientId, @RequestBody @Valid ClientPatchDto dto) {
        return clientMapper.map(clientService.patchClient(clientId, dto));
    }

}
