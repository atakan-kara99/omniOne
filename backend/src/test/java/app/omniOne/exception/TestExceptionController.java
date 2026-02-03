package app.omniOne.exception;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExceptionController {

    @GetMapping("/test/not-found")
    public void notFound() {
        throw new NoSuchResourceException("User not found");
    }

    @PostMapping("/test/validation")
    public void validation(@Valid @RequestBody GlobalExceptionHandlerTest.TestPayload payload) {
    }

    @GetMapping("/test/unexpected")
    public void unexpected() {
        throw new RuntimeException("Boom");
    }
}
