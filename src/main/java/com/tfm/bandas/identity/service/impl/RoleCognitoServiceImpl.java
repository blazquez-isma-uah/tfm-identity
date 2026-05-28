package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.config.CognitoProperties;
import com.tfm.bandas.identity.dto.RoleIdentityResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleIdentityService;
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
 * Implementación de RoleKeycloakService usando grupos de Amazon Cognito.
 * Activa únicamente en el perfil aws.
 * <p>
 * NOTA — IDs de grupos en Cognito:
 * En Keycloak los roles tienen un UUID independiente del nombre. En Cognito, los grupos
 * no tienen un UUID separado: el nombre del grupo ES su identificador único.
 * Por eso getRoleById(id) trata el parámetro id como nombre del grupo.
 * MS Users almacena el nombre como id, por lo que el comportamiento es transparente.
 */
@Service
@Profile("aws")
@RequiredArgsConstructor
public class RoleCognitoServiceImpl implements RoleIdentityService {

    private static final Logger logger = LoggerFactory.getLogger(RoleCognitoServiceImpl.class);

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties properties;
    private final CognitoUserHelper helper;

    @Override
    public List<RoleIdentityResponse> listAllRoles() {
        List<String> permitted = properties.listPermittedRoles();
        var response = cognitoClient.listGroups(ListGroupsRequest.builder()
                .userPoolId(properties.userPoolId())
                .build());

        return response.groups().stream()
                .filter(g -> permitted.stream().anyMatch(p -> g.groupName().startsWith(p)))
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleIdentityResponse getRoleByName(String roleName) {
        try {
            var response = cognitoClient.getGroup(GetGroupRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .groupName(roleName)
                    .build());
            return mapToRoleResponse(response.group());
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    @Override
    public RoleIdentityResponse getRoleById(String roleId) {
        // En Cognito el nombre del grupo ES el identificador — tratamos roleId como nombre
        return getRoleByName(roleId);
    }

    @Override
    public RoleIdentityResponse createRealmRole(RoleRegisterDTO dto) {
        cognitoClient.createGroup(CreateGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .groupName(dto.name())
                .description(dto.description())
                .build());
        logger.info("Group created in Cognito: {}", dto.name());
        return getRoleByName(dto.name());
    }

    @Override
    public void deleteRealmRole(String roleName) {
        cognitoClient.deleteGroup(DeleteGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .groupName(roleName)
                .build());
        logger.info("Group deleted from Cognito: {}", roleName);
    }

    @Override
    public List<RoleIdentityResponse> listUserRoles(String userId) {
        // userId = sub UUID → resolver a username para Cognito
        String username = helper.resolveUsernameFromSub(userId);
        List<String> permitted = properties.listPermittedRoles();

        var response = cognitoClient.adminListGroupsForUser(AdminListGroupsForUserRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .build());

        return response.groups().stream()
                .filter(g -> permitted.stream().anyMatch(p -> g.groupName().startsWith(p)))
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void assignRoleToUser(String userId, String roleName) {
        String username = helper.resolveUsernameFromSub(userId);
        cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .groupName(roleName)
                .build());
        logger.info("User {} added to group {} in Cognito", username, roleName);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleName) {
        String username = helper.resolveUsernameFromSub(userId);
        cognitoClient.adminRemoveUserFromGroup(AdminRemoveUserFromGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(username)
                .groupName(roleName)
                .build());
        logger.info("User {} removed from group {} in Cognito", username, roleName);
    }

    private RoleIdentityResponse mapToRoleResponse(GroupType group) {
        // En Cognito el id y el name son el mismo valor (nombre del grupo)
        return new RoleIdentityResponse(
                group.groupName(),
                group.groupName(),
                group.description()
        );
    }
}