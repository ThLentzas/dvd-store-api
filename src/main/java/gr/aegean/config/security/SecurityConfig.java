package gr.aegean.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import gr.aegean.exception.CustomAccessDeniedHandler;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

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