package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.client.RoleKeycloakApiClient;
import com.tfm.bandas.identity.config.KeycloakProperties;
import com.tfm.bandas.identity.dto.RoleIdentityResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("docker")
@RequiredArgsConstructor
public class RoleKeycloakServiceImpl implements RoleIdentityService {
    private final RoleKeycloakApiClient keycloakApiClient;
    private final KeycloakProperties properties;

    @Override
    public List<RoleIdentityResponse> listAllRoles() {
        String token = keycloakApiClient.getAdminToken();
        List<String> permitted = properties.listPermittedRoles();
        return keycloakApiClient.listAllRolesDto(token).stream()
                .filter(r -> permitted.stream().anyMatch(p -> r.name().startsWith(p)))
                .toList();
    }

    @Override
    public RoleIdentityResponse getRoleByName(String roleName) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getRealmRoleByNameDto(token, roleName);
    }

    @Override
    public RoleIdentityResponse getRoleById(String roleId) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getRealmRoleByIdDto(token, roleId);
    }

    @Override
    public RoleIdentityResponse createRealmRole(RoleRegisterDTO dto) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.createRealmRoleDto(token, dto);
    }

    @Override
    public void deleteRealmRole(String roleName) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteRealmRole(token, roleName);
    }

    @Override
    public List<RoleIdentityResponse> listUserRoles(String userId) {
        String token = keycloakApiClient.getAdminToken();
        List<String> permitted = properties.listPermittedRoles();
        return keycloakApiClient.listUserRolesDto(token, userId).stream()
                .filter(r -> permitted.stream().anyMatch(p -> r.name().startsWith(p)))
                .toList();
    }

    @Override
    public void assignRoleToUser(String userId, String roleName) {
        String token = keycloakApiClient.getAdminToken();
        var role = keycloakApiClient.getRealmRoleByName(token, roleName);
        keycloakApiClient.assignRoleToUser(token, userId, role);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleName) {
        String token = keycloakApiClient.getAdminToken();
        var role = keycloakApiClient.getRealmRoleByName(token, roleName);
        keycloakApiClient.removeRoleFromUser(token, userId, role);
    }
}
