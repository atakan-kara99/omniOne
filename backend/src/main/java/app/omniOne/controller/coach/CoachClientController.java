package app.omniOne.controller.coach;

import app.omniOne.auth.AuthService;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static app.omniOne.auth.AuthService.getMyId;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coach/clients/")
public class CoachClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final AuthService authService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ClientResponseDto> getClients() {
        return clientService.getClients(getMyId()).stream().map(clientMapper::map).toList();
    }

    @GetMapping("{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient(@PathVariable UUID clientId) {
        return clientMapper.map(clientService.getClient(getMyId(), clientId));
    }

    @GetMapping("/invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invite(@RequestParam @Email @NotBlank String email) {
        authService.sendInvitationMail(email, getMyId());
    }

}
