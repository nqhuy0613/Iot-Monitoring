package iot.backend.iotmonitoring.service.auth;

import iot.backend.iotmonitoring.dto.request.LoginRequest;
import iot.backend.iotmonitoring.dto.response.LoginResponse;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final AuthenticationManager authenticationManager =
            mock(AuthenticationManager.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final AuthService authService = new AuthService(
            authenticationManager,
            userRepository,
            jwtTokenService
    );

    @Test
    void returnsTokenAndUserWhenCredentialsAreValid() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFullName("Nguyen An");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken("testuser")).thenReturn("access-token");

        LoginResponse response = authService.login(
                new LoginRequest(" testuser ", "test123")
        );

        assertEquals("access-token", response.token());
        assertEquals(1L, response.user().id());
        assertEquals("testuser", response.user().username());
        assertEquals("Nguyen An", response.user().fullName());
        verify(jwtTokenService).generateAccessToken("testuser");
    }

    @Test
    void returnsUnauthorizedWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("testuser", "wrong"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
