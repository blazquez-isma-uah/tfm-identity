package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.dto.RoleIdentityResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;

import java.util.List;

public interface RoleIdentityService {
    List<RoleIdentityResponse> listAllRoles();
    RoleIdentityResponse getRoleByName(String roleName);
    RoleIdentityResponse getRoleById(String roleId);
    RoleIdentityResponse createRealmRole(RoleRegisterDTO dto);
    void deleteRealmRole(String roleName);
    List<RoleIdentityResponse> listUserRoles(String userId);
    void assignRoleToUser(String userId, String roleName);
    void removeRoleFromUser(String userId, String roleName);
}
