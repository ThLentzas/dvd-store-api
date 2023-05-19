package gr.aegean.service;

import gr.aegean.model.user.UserPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * Service responsible for generating JWT tokens for authenticated users. The JWT token contains the user's
 * authorities.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    /**
     * Generates a JWT token for the given user principal.

     * @param userPrincipal the user principal for whom to generate the JWT token.
     * @return the generated JWT token as a string.
     */
    public String assignToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        long expiresIn = 2;

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(userPrincipal.getUsername())
                .claim("authorities", authorities)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
