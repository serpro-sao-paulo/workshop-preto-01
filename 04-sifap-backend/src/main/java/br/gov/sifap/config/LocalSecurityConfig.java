package br.gov.sifap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for local development profile.
 * OAuth2/JWT is disabled — all requests are permitted without authentication.
 * This bean is only active when SPRING_PROFILES_ACTIVE=local.
 */
@Configuration
@EnableWebSecurity
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
