package com.tfm.bandas.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Modelo híbrido:
 * - El Gateway valida firma/exp de JWT
 * - Aquí validamos autoridad (p.ej. solo ADMIN puede crear)
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/actuator/**").permitAll()
              .requestMatchers(HttpMethod.POST, "/api/identity/keycloak/**").hasRole("ADMIN")
              .anyRequest().authenticated()

//              .requestMatchers(HttpMethod.POST, "/api/identity/keycloak/**").permitAll()
//              .requestMatchers(HttpMethod.GET, "/api/identity/keycloak/**").permitAll()
          )
          .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
