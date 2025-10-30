package com.tfm.bandas.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
        String authServerUrl,   // p.ej. http://keycloak:8080
        String realm,           // p.ej. tfm-bandas
        String adminClientId,   // p.ej. admin-cli o client propio
        String adminClientSecret,
        String permittedRoles // Roles permitidos para acceder a la API, separados por comas
) {
    public String tokenUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }
    public String adminBase() {
        return authServerUrl + "/admin/realms/" + realm;
    }
    public List<String> listPermittedRoles() {
        return List.of(permittedRoles.split(","));
    }
}
