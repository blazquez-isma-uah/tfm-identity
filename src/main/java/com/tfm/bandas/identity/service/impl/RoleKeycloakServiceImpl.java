package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.client.RoleKeycloakApiClient;
import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleKeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleKeycloakServiceImpl implements RoleKeycloakService {
    private final RoleKeycloakApiClient keycloakApiClient;

    @Override
    public List<KeycloakRoleResponse> listAllRoles() {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.listAllRolesDto(token);
    }

    @Override
    public KeycloakRoleResponse getRoleByName(String roleName) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getRealmRoleByNameDto(token, roleName);
    }

    @Override
    public KeycloakRoleResponse getRoleById(String roleId) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getRealmRoleByIdDto(token, roleId);
    }

    @Override
    public KeycloakRoleResponse createRealmRole(RoleRegisterDTO dto) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.createRealmRoleDto(token, dto);
    }

    @Override
    public void deleteRealmRole(String roleName) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteRealmRole(token, roleName);
    }

    @Override
    public List<KeycloakRoleResponse> listUserRoles(String userId) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.listUserRolesDto(token, userId);
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
