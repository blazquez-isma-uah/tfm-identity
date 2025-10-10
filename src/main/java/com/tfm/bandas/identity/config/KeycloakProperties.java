package com.tfm.bandas.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
        String authServerUrl,   // p.ej. http://keycloak:8080
        String realm,           // p.ej. tfm-bandas
        String adminClientId,   // p.ej. admin-cli o client propio
        String adminClientSecret
) {
    public String tokenUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }
    public String adminBase() {
        return authServerUrl + "/admin/realms/" + realm;
    }
}
