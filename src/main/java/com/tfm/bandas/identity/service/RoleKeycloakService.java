package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;

import java.util.List;

public interface RoleKeycloakService {
    List<KeycloakRoleResponse> listAllRoles();
    KeycloakRoleResponse getRoleByName(String roleName);
    KeycloakRoleResponse getRoleById(String roleId);
    KeycloakRoleResponse createRealmRole(RoleRegisterDTO dto);
    void deleteRealmRole(String roleName);
    List<KeycloakRoleResponse> listUserRoles(String userId);
    void assignRoleToUser(String userId, String roleName);
    void removeRoleFromUser(String userId, String roleName);
}
