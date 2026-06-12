package com.tfm.bandas.identity.config;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListGroupsRequest;

/**
 * Hook CRaC para SnapStart de AWS Lambda en el perfil aws.
 * <p>
 * SnapStart usa CRaC (Coordinated Restore at Checkpoint) internamente.
 * Con org.crac en el classpath, Lambda invoca beforeCheckpoint (antes de
 * guardar el snapshot) y afterRestore (tras restaurar el snapshot).
 * <p>
 * Sin CRaC, el primer cold start tras restore tardaba ~16s porque el cliente
 * HTTP de Netty (AWS SDK v2) quedaba con conexiones invalidas. El SDK intentaba
 * usarlas, fallaba con timeout, y reconectaba con backoff exponencial.
 * <p>
 * Solucion:
 *   - afterRestore: fuerza la recreacion del cliente Cognito via @RefreshScope
 *     y hace una llamada de calentamiento para establecer la conexion antes
 *     de que llegue la primera peticion real del usuario.
 * <p>
 * Spring Boot 3.5.x + Spring Security 6.5 gestionan automaticamente sus
 * propios hooks CRaC (DataSource, NimbusJwtDecoder JWKS cache) cuando
 * org.crac esta en el classpath. Solo necesitamos el hook para el AWS SDK v2.
 */
@Component
@Profile("aws")
public class SnapStartPrimingResource implements Resource {

    @Autowired
    private RefreshScope refreshScope;

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    private CognitoProperties cognitoProperties;

    public SnapStartPrimingResource() {
        // Registra este componente como Resource CRaC en el contexto global.
        // Lambda invocara los hooks de este componente en checkpoint y restore.
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        // No se necesita cerrar el cliente aqui. @RefreshScope lo destruira y recreara con un cliente fresco en afterRestore.
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {
        // 1. Destruye el cliente Cognito con conexiones Netty invalidas.
        //    RefreshScope.refresh() destruye el bean actual. La siguiente
        //    llamada al proxy lo recrea con un cliente HTTP completamente nuevo.
        refreshScope.refresh("cognitoClient");

        // 2. Llamada de calentamiento: fuerza la creacion del nuevo cliente
        //    y establece la conexion con Cognito ANTES de la primera peticion real.
        //    Sin esto, la creacion del cliente ocurre durante la primera peticion
        //    del usuario. Con esto, ocurre aqui (invisible al usuario).
        try {
            cognitoClient.listGroups(
                ListGroupsRequest.builder()
                    .userPoolId(cognitoProperties.userPoolId())
                    .limit(1)
                    .build()
            );
        } catch (Exception e) {
            // Si el calentamiento falla no es critico: la primera peticion
            // establecera la conexion con un overhead minimo.
        }
    }
}