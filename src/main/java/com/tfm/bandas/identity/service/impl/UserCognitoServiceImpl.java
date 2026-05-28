package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.config.CognitoProperties;
import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de UserKeycloakService usando Amazon Cognito SDK v2.
 * Activa únicamente en el perfil aws.
 *
 * NOTA IMPORTANTE — Username inmutable en Cognito:
 * En Keycloak el username se puede cambiar. En Cognito es inmutable una vez creado.
 * updateUserData acepta el parámetro username por compatibilidad de interfaz
 * pero lo ignora, actualizando únicamente email, given_name y family_name.
 * Esta es una limitación conocida documentada en el TFM.
 */
@Service
@Profile("aws")
@RequiredArgsConstructor
public class UserCognitoServiceImpl implements UserIdentityService {

    private static final Logger logger = LoggerFactory.getLogger(UserCognitoServiceImpl.class);

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties properties;
    private final CognitoUserHelper helper;

    @Override
    public UserIdentityResponse createUser(UserRegisterDTO dto) {
        // Paso 1: crear usuario (queda en estado FORCE_CHANGE_PASSWORD)
        var createRequest = AdminCreateUserRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(dto.username())
                .userAttributes(
                        AttributeType.builder().name("email").value(dto.email()).build(),
                        AttributeType.builder().name("email_verified").value("true").build(),
                        AttributeType.builder().name("given_name").value(dto.firstName()).build(),
                        AttributeType.builder().name("family_name").value(dto.lastName()).build()
                )
                // SUPPRESS evita que Cognito envíe email de bienvenida
                .messageAction(MessageActionType.SUPPRESS)
                .build();

        AdminCreateUserResponse createResponse = cognitoClient.adminCreateUser(createRequest);

        // Paso 2: establecer contraseña permanente (elimina FORCE_CHANGE_PASSWORD)
        cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(dto.username())
                .password(dto.password())
                .permanent(true)
                .build());

        // El sub es el identificador único del usuario en el JWT (equivale al iamId en Keycloak)
        String sub = helper.getAttribute(createResponse.user(), "sub")
                .orElseThrow(() -> new RuntimeException("Cognito did not return sub attribute after user creation"));

        logger.info("User created in Cognito — username: {}, sub: {}", dto.username(), sub);
        return new UserIdentityResponse(sub, dto.username());
    }

    @Override
    public void deleteUser(String userId) {
        // userId = sub UUID del JWT → necesitamos resolver a username para Cognito
        String username = helper.resolveUsernameFromSub(userId);
        cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .build());
        logger.info("User deleted from Cognito — sub: {}, username: {}", userId, username);
    }

    @Override
    public void deleteUserByUsername(String username) {
        cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .build());
        logger.info("User deleted from Cognito by username: {}", username);
    }

    @Override
    public void updateUserPassword(String userId, String newPassword) {
        String username = helper.resolveUsernameFromSub(userId);
        cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .password(newPassword)
                .permanent(true)
                .build());
        logger.info("Password updated in Cognito — sub: {}", userId);
    }

    @Override
    public UserIdentityResponse updateUserData(String userId, String email, String firstName, String lastName) {
        String cognitoUsername = helper.resolveUsernameFromSub(userId);

        cognitoClient.adminUpdateUserAttributes(AdminUpdateUserAttributesRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(cognitoUsername)
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("email_verified").value("true").build(),
                        AttributeType.builder().name("given_name").value(firstName).build(),
                        AttributeType.builder().name("family_name").value(lastName).build()
                )
                .build());

        logger.info("User data updated in Cognito — sub: {}", userId);
        return new UserIdentityResponse(userId, cognitoUsername);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        try {
            cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(username)
                    .build());
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean userExistsByEmail(String email) {
        var response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(properties.userPoolId())
                .filter("email = \"" + email + "\"")
                .limit(1)
                .build());
        return !response.users().isEmpty();
    }

    @Override
    public UserIdentityResponse getUserByUsername(String username) {
        try {
            var response = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(username)
                    .build());

            String sub = response.userAttributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElse(username); // fallback defensivo

            return new UserIdentityResponse(sub, username);
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    @Override
    public UserIdentityResponse getUserById(String userId) {
        // userId = sub UUID → listUsers con filtro por atributo sub
        var response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(properties.userPoolId())
                .filter("sub = \"" + userId + "\"")
                .limit(1)
                .build());

        if (response.users().isEmpty()) return null;

        var user = response.users().getFirst();
        return new UserIdentityResponse(userId, user.username());
    }

    @Override
    public UserIdentityDetailsResponse getUserDetailsById(String userId) {
        var response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(properties.userPoolId())
                .filter("sub = \"" + userId + "\"")
                .limit(1)
                .build());

        if (response.users().isEmpty()) return null;

        var user = response.users().getFirst();
        return new UserIdentityDetailsResponse(
                userId,
                user.username(),
                helper.getAttribute(user, "email").orElse(null),
                helper.getAttribute(user, "given_name").orElse(null),
                helper.getAttribute(user, "family_name").orElse(null)
        );
    }

    @Override
    public UserIdentityDetailsResponse getUserDetailsByUsername(String username) {
        try {
            var response = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(username)
                    .build());

            String sub = response.userAttributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElse(null);

            String email = response.userAttributes().stream()
                    .filter(a -> a.name().equals("email"))
                    .map(AttributeType::value).findFirst().orElse(null);
            String firstName = response.userAttributes().stream()
                    .filter(a -> a.name().equals("given_name"))
                    .map(AttributeType::value).findFirst().orElse(null);
            String lastName = response.userAttributes().stream()
                    .filter(a -> a.name().equals("family_name"))
                    .map(AttributeType::value).findFirst().orElse(null);

            return new UserIdentityDetailsResponse(sub, username, email, firstName, lastName);
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<UserIdentityResponse> listAllUsers() {
        // Cognito pagina a 60 usuarios por llamada.
        // Para el volumen de una banda amateur (< 60 músicos) una página es suficiente.
        // Limitación documentada: si la banda supera 60 miembros, habría que implementar paginación.
        var response = cognitoClient.listUsers(ListUsersRequest.builder()
                .userPoolId(properties.userPoolId())
                .build());

        return response.users().stream()
                .map(user -> {
                    String sub = helper.getAttribute(user, "sub").orElse(user.username());
                    return new UserIdentityResponse(sub, user.username());
                })
                .collect(Collectors.toList());
    }
}