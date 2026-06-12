package com.tfm.bandas.identity.service.impl;

import com.tfm.bandas.identity.client.UserKeycloakApiClient;
import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Profile("docker")
public class UserKeycloakServiceImpl implements UserIdentityService {
    private final UserKeycloakApiClient keycloakApiClient;

    @Override
    public UserIdentityResponse createUser(UserRegisterDTO req) {
        String token = keycloakApiClient.getAdminToken();
        String userId = keycloakApiClient.createUser(token, req);
        return new UserIdentityResponse(userId, req.username());
    }

    @Override
    public void deleteUser(String userId) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteUser(token, userId);
    }

    @Override
    public void deleteUserByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteUserByUsername(token, username);
    }

    @Override
    public void updateUserPassword(String userId, String newPassword) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.updateUserPassword(token, userId, newPassword);
    }

    @Override
    public UserIdentityResponse updateUserData(String userId, String email, String firstName, String lastName) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.updateUserData(token, userId, email, firstName, lastName);

        // Para devolver el username (como hace el resto de endpoints), recuperamos el usuario.
        var updated = keycloakApiClient.getUserById(token, userId);
        String username = updated != null ? (String) updated.get("username") : null;
        return new UserIdentityResponse(userId, username);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.userExistsByUsername(token, username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.userExistsByEmail(token, email);
    }

    @Override
    public UserIdentityResponse getUserByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        var user = keycloakApiClient.getUserByUsername(token, username);
        if (user == null) return null;
        return new UserIdentityResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public UserIdentityResponse getUserById(String userId) {
        String token = keycloakApiClient.getAdminToken();
        var user = keycloakApiClient.getUserById(token, userId);
        if (user == null) return null;
        return new UserIdentityResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public UserIdentityDetailsResponse getUserDetailsById(String userId) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getUserDetailsById(token, userId);
    }

    @Override
    public UserIdentityDetailsResponse getUserDetailsByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getUserDetailsByUsername(token, username);
    }

    @Override
    public List<UserIdentityResponse> listAllUsers() {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.listAllUsers(token);
    }
}
