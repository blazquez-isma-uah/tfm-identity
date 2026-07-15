package com.tfm.bandas.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    // Identity no tiene CORS: no es accesible desde el exterior ni desde el frontend.
    // Solo MS Users lo llama internamente desde la red Docker/VPC.
    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwtAuthConverter);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Se usan PathPatternRequestMatcher explicitos en lugar de
                        // requestMatchers(String). Con String, Spring Security 6.x crea
                        // un MvcRequestMatcher que introspecciona los mappings del servlet.
                        // En el contenedor serverless de Lambda, ServletRegistration.getMappings()
                        // devuelve null y provoca un NullPointerException en
                        // ServletRegistrationsSupport. PathPatternRequestMatcher hace matching
                        // directo del path sin esa introspeccion. Funciona igual en docker
                        // (Tomcat) y en Lambda.
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/actuator/**")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/identity/**")).hasRole("ADMIN")
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.PUT, "/api/identity/**")).hasAnyRole("ADMIN", "MUSICIAN")
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.DELETE, "/api/identity/**")).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(conv)));

        return http.build();
    }
}