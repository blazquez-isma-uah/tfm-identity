package com.tfm.bandas.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Properties de Amazon Cognito para el perfil aws.
 * Equivalente funcional de KeycloakProperties para el perfil docker.
 * Se registra en AppConfigAws, activo únicamente con @Profile("aws").
 */
@ConfigurationProperties(prefix = "cognito")
public record CognitoProperties(
        String userPoolId,
        String region,
        String permittedRoles
) {
    public List<String> listPermittedRoles() {
        return List.of(permittedRoles.split(","));
    }
}