package com.tfm.bandas.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Modelo híbrido:
 * - El Gateway valida firma/exp de JWT
 * - Aquí validamos autoridad (p.ej. solo ADMIN puede crear)
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractRealmRoles);

        http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/actuator/**").permitAll()
              .requestMatchers(HttpMethod.POST, "/api/identity/keycloak/**").hasRole("ADMIN")
              .anyRequest().authenticated()

//              .requestMatchers(HttpMethod.POST, "/api/identity/keycloak/**").permitAll()
//              .requestMatchers(HttpMethod.GET, "/api/identity/keycloak/**").permitAll()
          )
          .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(conv)));

        return http.build();
    }

    /**
     * Extrae los roles del realm del token JWT de Keycloak (realm_access.roles) y los convierte
     * en una colección de GrantedAuthority con el prefijo "ROLE_". Esto permite que Spring Security
     * reconozca y utilice estos roles para la autorización basada en roles.
     * @param jwt
     * @return
     */
    private static Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        var out = new HashSet<SimpleGrantedAuthority>();
        var realm = jwt.getClaimAsMap("realm_access");
        if (realm != null && realm.get("roles") instanceof List<?> roles) {
            for (Object r : roles) out.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
        }
        return new HashSet<GrantedAuthority>(out);
    }
}
