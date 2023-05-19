package gr.aegean.service;

import gr.aegean.model.user.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.model.user.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private JwtEncoder jwtEncoder;
    private JwtService underTest;

    @BeforeEach
    void setup() {
        underTest = new JwtService(jwtEncoder);
    }

    @Test
    void shouldAssignToken() {
        //Arrange
        UserPrincipal userPrincipal = new UserPrincipal(new User("test",
                "test", UserRole.ROLE_EMPLOYEE));
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        String jwtToken = "jwtToken";
        Instant now = Instant.now();
        long expiresIn = 2;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(userPrincipal.getUsername())
                .claim("authorities", authorities)
                .build();

        Map<String, Object> headers = Map.of("alg", "RS256", "typ", "JWT");

        Jwt jwt = new Jwt(jwtToken, claims.getIssuedAt(), claims.getExpiresAt(),
                 headers, claims.getClaims());

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        //Act
        String assignedToken = underTest.assignToken(userPrincipal);

        //Assert
        assertThat(assignedToken).isNotNull();
        assertThat(assignedToken).isEqualTo(jwt.getTokenValue());

        verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
    }
}