package app.omniOne.controller.coach;

import app.omniOne.authentication.AuthService;
import app.omniOne.model.dto.ClientResponse;
import app.omniOne.model.dto.InvitationRequest;
import app.omniOne.service.ClientService;
import app.omniOne.service.CoachingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@Tag(name = "Coach - Client")
@RequiredArgsConstructor
@RequestMapping("/coach/clients")
public class CoachClientController {

    private final AuthService authService;
    private final ClientService clientService;
    private final CoachingService coachingService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClientResponse> getClients() {
        return clientService.getClients(getMyId());
    }

    @GetMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@authService.isCoachedByMe(#clientId)")
    public ClientResponse getClient(@PathVariable UUID clientId) {
        return clientService.getClient(clientId);
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invite(@RequestBody @Valid InvitationRequest request) {
        authService.sendInvitationMail(request.email(), getMyId());
    }

    @DeleteMapping("/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authService.isCoachedByMe(#clientId)")
    public void endCoaching(@PathVariable UUID clientId) {
        coachingService.endCoaching(clientId);
    }

}
