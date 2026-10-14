package iot.backend.iotmonitoring.security;

import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final DatabaseUserDetailsService userDetailsService =
            new DatabaseUserDetailsService(userRepository);

    @Test
    void loadsAuthenticationDataFromUserTable() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-password");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("alice");

        assertEquals("alice", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
    }

    @Test
    void rejectsUnknownUsername() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing")
        );
    }
}
