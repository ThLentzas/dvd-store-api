package gr.aegean.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import gr.aegean.exception.CustomAccessDeniedHandler;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll();
                    /*
                        In "/api/v1/dvds/**" => ** represents zero or more directories. In the case of the request
                        "/api/v1/dvds?title=title" we have 0 subdirectories, so it works. It doesn't mean anything
                        after '/' in that case "/api/v1/dvds?title=title" would fail cause there is no '/' after
                        dvds
                     */
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