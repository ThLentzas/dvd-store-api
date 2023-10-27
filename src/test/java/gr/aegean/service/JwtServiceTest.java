package gr.aegean.service;

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

import gr.aegean.entity.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.model.user.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
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
        User user = new User(
                1,
                "Employee",
                "Employee",
                "employee@gmail.com",
                "test",
                UserRole.ROLE_EMPLOYEE);
        UserPrincipal userPrincipal = new UserPrincipal(user);
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
                .subject(userPrincipal.user().getId().toString())
                .claim("authorities", authorities)
                .build();

        Map<String, Object> headers = Map.of(
                "alg", "RS256",
                "typ", "JWT");

        Jwt expected = new Jwt(
                jwtToken,
                claims.getIssuedAt(),
                claims.getExpiresAt(),
                headers,
                claims.getClaims());

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(expected);

        //Act
        String actual = underTest.assignToken(userPrincipal);

        //Assert
        assertThat(actual).isEqualTo(expected.getTokenValue());
    }
}