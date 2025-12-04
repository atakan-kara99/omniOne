package app.omniOne.controller;

import app.omniOne.model.dto.ClientPostDto;
import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coach/{coachId}")
public class CoachClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @PostMapping("/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponseDto registerClient(@PathVariable Long coachId, @RequestBody @Valid ClientPostDto dto) {
        return clientMapper.map(clientService.registerClient(coachId, dto));
    }

    @GetMapping("/clients")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientResponseDto> getClients(@PathVariable Long coachId) {
        return clientService.getClients(coachId).stream().map(clientMapper::map).toList();
    }

    @GetMapping("/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient(@PathVariable Long coachId, @PathVariable Long clientId) {
        return clientMapper.map(clientService.getClient(coachId, clientId));
    }

}
