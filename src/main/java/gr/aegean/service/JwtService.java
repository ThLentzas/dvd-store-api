package gr.aegean.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import gr.aegean.model.user.UserPrincipal;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    /**
     * Role - Based JwtToken
     * The claims of the JwtToken are: issuer, when it is issued at, when it expires at, subject(user's id) and
     * a custom claim for the user's authorities.
     */
    public String assignToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        long expiresIn = 2;

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet
                .builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(userPrincipal.user().getId().toString())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
