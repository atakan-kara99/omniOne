package app.omniOne.service;

import app.omniOne.exception.NotAllowedException;
import app.omniOne.model.dto.ChangePasswordRequest;
import app.omniOne.model.dto.UserProfileRequest;
import app.omniOne.model.entity.User;
import app.omniOne.model.entity.UserProfile;
import app.omniOne.model.mapper.UserMapper;
import app.omniOne.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public User getUser(UUID id) {
        return userRepo.findByIdOrThrow(id);
    }

    public User changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepo.findByIdOrThrow(id);
        if (!encoder.matches(request.oldPassword(), user.getPassword()))
            throw new NotAllowedException("Old password is incorrect");
        user.setPassword(encoder.encode(request.newPassword()));
        return userRepo.save(user);
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
        return userRepo.save(user).getProfile();
    }

    public UserProfile getProfile(UUID id) {
        User user = userRepo.findByIdOrThrow(id);
        return user.getProfile();
    }
}
