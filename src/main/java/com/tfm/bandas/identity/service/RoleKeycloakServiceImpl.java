package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.client.RoleKeycloakApiClient;
import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleKeycloakServiceImpl implements RoleKeycloakService {
    private final RoleKeycloakApiClient kc;

    @Override
    public List<KeycloakRoleResponse> listAllRoles() {
        String token = kc.getAdminToken();
        return kc.listAllRolesDto(token);
    }

    @Override
    public KeycloakRoleResponse getRoleByName(String roleName) {
        String token = kc.getAdminToken();
        return kc.getRealmRoleByNameDto(token, roleName);
    }

    @Override
    public KeycloakRoleResponse getRoleById(String roleId) {
        String token = kc.getAdminToken();
        return kc.getRealmRoleByIdDto(token, roleId);
    }

    @Override
    public void createRealmRole(String roleName) {
        String token = kc.getAdminToken();
        kc.createRealmRole(token, roleName);
    }

    @Override
    public void deleteRealmRole(String roleName) {
        String token = kc.getAdminToken();
        kc.deleteRealmRole(token, roleName);
    }

    @Override
    public List<KeycloakRoleResponse> listUserRoles(String userId) {
        String token = kc.getAdminToken();
        return kc.listUserRolesDto(token, userId);
    }

    @Override
    public void assignRealmRole(String userId, String roleName) {
        String token = kc.getAdminToken();
        var role = kc.getRealmRoleByName(token, roleName);
        kc.assignRealmRole(token, userId, role);
    }

    @Override
    public void removeRealmRole(String userId, String roleName) {
        String token = kc.getAdminToken();
        var role = kc.getRealmRoleByName(token, roleName);
        kc.removeRealmRole(token, userId, role);
    }
}
