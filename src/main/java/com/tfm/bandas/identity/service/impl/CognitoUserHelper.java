package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.config.CognitoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.Optional;

/**
 * Helper compartido por UserCognitoServiceImpl y RoleCognitoServiceImpl.
 * <p>
 * Problema: la mayoría de operaciones admin de Cognito usan el username como
 * identificador, pero MS Users almacena el sub (UUID del JWT) como iamId y lo
 * pasa como userId en las llamadas a MS Identity.
 * <p>
 * Este helper resuelve sub → username mediante listUsers con filtro por atributo sub.
 */
@Component
@Profile("aws")
@RequiredArgsConstructor
public class CognitoUserHelper {

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties properties;

    /**
     * Resuelve el username de Cognito a partir del sub (UUID del JWT).
     * Usa listUsers con filtro por atributo sub — soportado nativamente por Cognito.
     */
    public String resolveUsernameFromSub(String sub) {
        var request = ListUsersRequest.builder()
                .userPoolId(properties.userPoolId())
                .filter("sub = \"" + sub + "\"")
                .limit(1)
                .build();

        var response = cognitoClient.listUsers(request);

        if (response.users().isEmpty()) {
            throw new RuntimeException("No Cognito user found with sub: " + sub);
        }
        return response.users().getFirst().username();
    }

    /**
     * Extrae el valor de un atributo estándar de un UserType de Cognito.
     */
    public Optional<String> getAttribute(UserType user, String attributeName) {
        return user.attributes().stream()
                .filter(a -> a.name().equals(attributeName))
                .map(AttributeType::value)
                .findFirst();
    }
}