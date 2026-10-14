package iot.backend.iotmonitoring.service.auth;

import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = new CurrentUserService(userRepository);

    @Test
    void resolvesCurrentUserFromAuthenticatedPrincipal() {
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        Principal principal = () -> "alice";
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertEquals(user, currentUserService.requireCurrentUser(principal));
    }

    @Test
    void rejectsRequestWithoutAuthenticatedPrincipal() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> currentUserService.requireCurrentUser(null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
