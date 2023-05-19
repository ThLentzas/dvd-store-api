package gr.aegean.config;

import gr.aegean.exception.CustomAccessDeniedHandler;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Defines the security filter chain used by Spring Security to secure the endpoints of the application.
 * It sets up authentication and authorization rules and creates a SecurityFilterChain.
 * The authentication is handled by an AuthenticationProvider, and JWT tokens are used for stateless authentication.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Configures the security filter chain to be used by Spring Security to secure the endpoints of the application.
     * Authentication and authorization rules are set up here.
     *
     * @param http HttpSecurity object used to configure the security filter chain.
     * @return a SecurityFilterChain object representing the configured security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers( "/api/v1/auth/**").permitAll();
                    auth.requestMatchers("/api/v1/dvds/**").hasAuthority("ROLE_EMPLOYEE");
                })
                .csrf(AbstractHttpConfigurer:: disable)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt()
                        .and()
                        .accessDeniedHandler(customAccessDeniedHandler))
                .formLogin(AbstractHttpConfigurer:: disable)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .build();
    }
}

