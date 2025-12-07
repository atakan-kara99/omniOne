package app.omniOne.auth;

import app.omniOne.auth.model.UserDetails;
import app.omniOne.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new UserDetails(
                userRepo.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }

}
