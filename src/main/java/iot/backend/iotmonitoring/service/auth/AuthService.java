package iot.backend.iotmonitoring.service.auth;

import iot.backend.iotmonitoring.dto.request.LoginRequest;
import iot.backend.iotmonitoring.dto.response.AuthenticatedUserResponse;
import iot.backend.iotmonitoring.dto.response.LoginResponse;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        authenticate(username, request.password());

        User user = userRepository.findByUsername(username)
                .orElseThrow(this::invalidCredentials);
        String token = jwtTokenService.generateAccessToken(user.getUsername());

        return new LoginResponse(
                token,
                new AuthenticatedUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName()
                )
        );
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            username,
                            password
                    )
            );
        } catch (AuthenticationException exception) {
            throw invalidCredentials();
        }
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }
}
