package app.omniOne;

import app.omniOne.authentication.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

/**
 * Shared utility for tests that need to mock the static AuthService#getMyId call.
 * Centralizing this avoids duplicated setup/teardown code across controller tests.
 */
public abstract class AuthTestSupport {

    protected MockedStatic<AuthService> authStatic;

    protected void mockAuthenticatedUser(UUID userId) {
        closeAuthMock();
        authStatic = Mockito.mockStatic(AuthService.class);
        authStatic.when(AuthService::getMyId).thenReturn(userId);
    }

    @AfterEach
    void closeAuthMock() {
        if (authStatic != null) {
            authStatic.close();
            authStatic = null;
        }
    }
}
