package app.omniOne.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class JwtController {

    private final JwtService jwtService;

//    @PostMapping("/login")
//    @ResponseStatus(HttpStatus.OK)
//    public JwtResponse loginUser(@RequestBody @Valid UserLoginDto dto) {
//        return jwtService.login(dto);
//    }

}
