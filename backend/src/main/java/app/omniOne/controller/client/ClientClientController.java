package app.omniOne.controller.client;

import app.omniOne.model.dto.ClientResponse;
import app.omniOne.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Client")
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientClientController {

    private final ClientService clientService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ClientResponse getClient() {
        return clientService.getClient(getMyId());
    }

//    @PatchMapping
//    @ResponseStatus(HttpStatus.OK)
//    public ClientResponse patchClient(@RequestBody @Valid ClientPatchRequest dto) {
//        return clientService.patchClient(getMyId(), dto);
//    }

}
