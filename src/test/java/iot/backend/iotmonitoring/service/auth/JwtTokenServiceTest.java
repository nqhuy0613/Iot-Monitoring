package iot.backend.iotmonitoring.service.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private static final String ISSUER = "http://iot-monitoring.local";
    private static final String SECRET =
            "iot-monitoring-test-secret-key-with-at-least-32-bytes";

    @Test
    void generatesSignedTokenWithUsernameAsSubject() {
        SecretKey secretKey = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        NimbusJwtEncoder jwtEncoder = NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        JwtTokenService tokenService = new JwtTokenService(jwtEncoder, ISSUER, 3600);

        String token = tokenService.generateAccessToken("testuser");
        Jwt decodedToken = jwtDecoder.decode(token);

        assertEquals("testuser", decodedToken.getSubject());
        assertEquals(ISSUER, decodedToken.getIssuer().toString());
        assertTrue(decodedToken.getExpiresAt().isAfter(decodedToken.getIssuedAt()));
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtTokenService(
                        mockJwtEncoder(),
                        ISSUER,
                        0
                )
        );
    }

    private org.springframework.security.oauth2.jwt.JwtEncoder mockJwtEncoder() {
        return parameters -> null;
    }
}
