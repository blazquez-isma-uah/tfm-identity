package com.tfm.bandas.identity.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * Configuración de infraestructura AWS activa únicamente en el perfil aws.
 * Equivalente a AppConfig (que crea el WebClient para Keycloak) pero para Cognito.
 * <p>
 * DefaultCredentialsProvider resuelve credenciales automáticamente:
 * - En Lambda: usa el IAM execution role asignado a la función (sin configuración adicional)
 * - En local con perfil aws activo: usa ~/.aws/credentials o variables de entorno AWS_*
 */
@Configuration
@Profile("aws")
@EnableConfigurationProperties(CognitoProperties.class)
public class AppConfigAws {

    @Bean
    @RefreshScope
    public CognitoIdentityProviderClient cognitoClient(CognitoProperties properties) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}