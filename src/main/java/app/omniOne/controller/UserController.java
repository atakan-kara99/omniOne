package app.omniOne.controller;

import app.omniOne.model.dto.ChangePasswordRequest;
import app.omniOne.model.dto.UserDto;
import app.omniOne.model.dto.UserProfileDto;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static app.omniOne.authentication.AuthService.getMyId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser() {
        return userMapper.map(userService.getUser(getMyId()));
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public UserDto changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userMapper.map(userService.changePassword(getMyId(), request));
    }

    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileDto getProfile() {
        return userMapper.map(userService.getProfile(getMyId()));
    }

    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileDto putProfile(@RequestBody @Valid UserProfileRequest request) {
        return userMapper.map(userService.putProfile(getMyId(), request));
    }
}
