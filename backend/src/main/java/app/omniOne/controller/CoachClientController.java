package app.omniOne.controller;

import app.omniOne.model.dto.ClientResponseDto;
import app.omniOne.model.mapper.ClientMapper;
import app.omniOne.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coach/{coachId}")
@PreAuthorize("@authService.isOwner(#coachId)")
public class CoachClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

//    @PostMapping("/clients")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ClientResponseDto registerClient(@PathVariable UUID coachId, @RequestBody @Valid ClientPostDto dto) {
//        return clientMapper.map(clientService.registerClient(coachId, dto));
//    }

    @GetMapping("/clients")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientResponseDto> getClients(@PathVariable UUID coachId) {
        return clientService.getClients(coachId).stream().map(clientMapper::map).toList();
    }

    @GetMapping("/clients/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    public ClientResponseDto getClient(@PathVariable UUID coachId, @PathVariable UUID clientId) {
        return clientMapper.map(clientService.getClient(coachId, clientId));
    }

}
