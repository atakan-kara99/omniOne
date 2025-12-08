package app.omniOne.controller.client;

import app.omniOne.model.dto.ClientPatchRequest;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientClientController {

    private final ClientMapper clientMapper;
    private final ClientService clientService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient() {
        return clientMapper.map(clientService.getClient(getMyId()));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto patchClient(@RequestBody @Valid ClientPatchRequest dto) {
        return clientMapper.map(clientService.patchClient(getMyId(), dto));
    }

}
