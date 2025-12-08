package app.omniOne.service;

import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.dto.ChangePasswordRequest;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public User getUser(UUID id) {
        User user = userRepo.findByIdOrThrow(id);
        log.info("Successfully retrieved User {}", id);
        return user;
    }

    public User changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepo.findByIdOrThrow(id);
        if (!encoder.matches(request.oldPassword(), user.getPassword()))
            throw new NotAllowedException("Old password is incorrect");
        user.setPassword(encoder.encode(request.newPassword()));
        User savedUser = userRepo.save(user);
        log.info("Successfully changed password for User {}", id);
        return savedUser;
    }

    public UserProfile putProfile(UUID id, UserProfileRequest request) {
        User user = userRepo.findByIdOrThrow(id);
        UserProfile profile;
        if (user.getProfile() == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        } else {
            profile = user.getProfile();
        }
        userMapper.map(request, profile);
        UserProfile savedUserProfile = userRepo.save(user).getProfile();
        log.info("Successfully updated UserProfile for User {}", id);
        return savedUserProfile;
    }

    public UserProfile getProfile(UUID id) {
        User user = userRepo.findByIdOrThrow(id);
        UserProfile profile = user.getProfile();
        log.info("Successfully retrieved UserProfile for User {}", id);
        return profile;
    }

}
